package com.revolut.interview;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.jooby.Env;
import org.jooby.Err;
import org.jooby.MediaType;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Results;
import org.jooby.funzy.Try;

import static org.jooby.Err.DefHandler.VIEW;

/**
 * Same as default {@link Err.DefHandler} but with json error formatting
 */
@Slf4j
public class ErrorHandler implements Err.Handler {

    @Override
    public void handle(Request req, Response rsp, Err ex) throws Throwable {
        log.error("execution of: {}{} resulted in exception\nRoute:\n{}\n\nStacktrace:",
                req.method(), req.path(), req.route().print(6), ex);
        Config conf = req.require(Config.class);
        boolean stacktrace = Try.apply(() -> conf.getBoolean("err.stacktrace"))
                .orElse(req.require(Env.class).name().equals("dev"));
        rsp.send(
                Results.when(MediaType.json, () -> ex.toMap(stacktrace))
                        .when(MediaType.html, () -> Results.html(VIEW).put("err", ex.toMap(stacktrace)))
                        .when(MediaType.all, () -> ex.toMap(stacktrace)));
    }
}
