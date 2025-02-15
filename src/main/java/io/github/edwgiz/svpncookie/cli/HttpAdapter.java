package io.github.edwgiz.svpncookie.cli;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.compile;

final class HttpAdapter implements AutoCloseable {

    private final HttpOperations httpOperations = new HttpOperations();


    String parseRedirectPage(String loginUrl) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(loginUrl)).GET().build();
        return httpOperations.send(req,
                (response, body, contentLength) -> find(
                        body,
                        compile("window.location\\s*=\\s*'(.+?)'"),
                        contentLength));
    }


    String sendLoginPasswordForm(String uri, String user, String password) {
        final var formData = "AuthMethod=FormsAuthentication" +
                "&UserName=" + URLEncoder.encode(user, UTF_8) +
                "&Password=" + URLEncoder.encode(password, UTF_8);

        final var req = HttpRequest.newBuilder().uri(URI.create(uri)).POST(ofString(formData)).build();
        return httpOperations.send(req, (response, body, contentLength) -> {
            final var inputPinPattern = compile("<input (?:id|name)=\"pin\"");
            if (body.findWithinHorizon(inputPinPattern, contentLength - 1) != null) {
                final var formContextPattern = compile("name=\\s*\"Context\"\\s+value=\\s*\"(.+?)\"");
                return find(body, formContextPattern, contentLength);
            } else {
                return null;
            }
        });
    }


    HiddenForm sendPinForm(String uri, String context, String pin) {
        final var formData = "Continue=Continue&pin=" + pin + "&Context=" + URLEncoder.encode(context, UTF_8);
        final var req = HttpRequest.newBuilder().uri(URI.create(uri)).POST(ofString(formData)).build();
        return httpOperations.send(req, (response, bodyScanner, contentLength) -> {
            final var hiddenFormActionPattern = compile("name\\s*=\\s*\"hiddenform\"\\s+action\\s*=\\s*\"(.+?)\"");
            final var action = find(bodyScanner, hiddenFormActionPattern, contentLength);
            if (action == null) return null;
            final var hiddenFormSamlresponsePattern = compile("name\\s*=\\s*\"SAMLResponse\"\\s+value\\s*=\\s*\"(.+?)\"");
            final var samlResponse = find(bodyScanner, hiddenFormSamlresponsePattern, contentLength);
            if (samlResponse == null) return null;
            final var hiddenFormRelaystatePattern = compile("name\\s*=\\s*\"RelayState\"\\s+value\\s*=\\s*\"(.+?)\"");
            final var relayState = find(bodyScanner, hiddenFormRelaystatePattern, contentLength);
            if (relayState == null) return null;
            return new HiddenForm(action, samlResponse, relayState);
        });
    }


    String sendHiddenForm(HiddenForm form) {
        final var formData = "Continue=Continue" +
                "&SAMLResponse=" + URLEncoder.encode(form.samlResponse(), UTF_8) +
                "&RelayState=" + URLEncoder.encode(form.relayState(), UTF_8);

        final var req = HttpRequest.newBuilder().uri(URI.create(form.action())).POST(ofString(formData)).build();
        return httpOperations.send(req, (resp) -> {
            final var cookies = resp.headers().allValues("set-cookie");
            for (final var cookie : cookies) {
                final var svpncookiePattern = compile("SVPNCOOKIE\\s*=\\s*(.+?);");
                final var m = svpncookiePattern.matcher(cookie);
                if (m.find()) {
                    return m.group(1);
                }
            }
            return null;
        });
    }


    private String find(Scanner body, Pattern p, int contentLength) {
        if (body.findWithinHorizon(p, contentLength - 1) != null) {
            final var m = body.match();
            if (m.hasMatch()) {
                return m.group(1);
            }
        }
        return null;
    }


    @Override
    public void close() {
        httpOperations.close();
    }
}
