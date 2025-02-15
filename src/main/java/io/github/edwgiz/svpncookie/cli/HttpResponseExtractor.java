package io.github.edwgiz.svpncookie.cli;

import java.net.http.HttpResponse;
import java.util.Scanner;


@FunctionalInterface
interface HttpResponseExtractor<R> {
    R apply(HttpResponse<?> response, Scanner bodyScanner, int contentLength);
}
