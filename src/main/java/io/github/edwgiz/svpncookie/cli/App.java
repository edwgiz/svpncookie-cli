package io.github.edwgiz.svpncookie.cli;

import static java.lang.System.err;
import static java.lang.System.out;


public final class App {


    public static void main(String[] args) {
        System.exit(printSvpnCookie(args[0], args[1], args[2], args[3]));
    }


    private static int printSvpnCookie(String loginUrl, String user, String password, String pin) {
        try (final var a = new HttpAdapter()) {
            final var samlUrl = a.parseRedirectPage(loginUrl);
            if (samlUrl == null) {
                err.println("Can't find SAML redirect");
                return 1;
            }

            final var context = a.sendLoginPasswordForm(samlUrl, user, password);
            if (context == null) {
                err.println("Can't find PIN form");
                return 1;
            }

            final var hiddenForm = a.sendPinForm(samlUrl, context, pin);
            if (hiddenForm == null) {
                err.println("Can't find hidden form");
                return 1;
            }

            final var svpnCookie = a.sendHiddenForm(hiddenForm);
            if (svpnCookie == null) {
                err.println("Can't find SVPNCOOKIE");
                return 1;
            }

            out.println(svpnCookie);
        }
        return 0;
    }
}
