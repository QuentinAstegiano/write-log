package org.astegiano.wl;

import org.astegiano.wl.api.LogEntryManager;
import org.astegiano.wl.data.LogEntry;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.registry.Registry;
import ratpack.server.RatpackServer;

import java.util.Optional;

/**
 * Created by Quentin Astegiano on 19/10/2015.
 */
public class Server {

    public static class NotFound implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            ctx.next(Registry.single(Renderable.class, new Renderable() {
                @Override
                public int getStatus() {
                    return 404;
                }

                @Override
                public String getContent() {
                    return "Page not found !";
                }

                @Override
                public String getPageTitle() {
                    return "Page not found";
                }
            }));
        }
    }

    public static class LogEntryRenderer implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            final LogEntry logEntry = ctx.get(LogEntry.class);
            ctx.next(Registry.single(Renderable.class, new Renderable() {
                @Override
                public int getStatus() {
                    return 200;
                }

                @Override
                public String getContent() {
                    return "<h1>" + logEntry.title + "</h1><div>" + logEntry.content + "</div>";
                }

                @Override
                public String getPageTitle() {
                    return logEntry.title;
                }
            }));
        }
    }

    public static class Renderer implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            Renderable renderable = ctx.get(Renderable.class);
            ctx.getResponse().status(renderable.getStatus()).send(renderable.getContent());
        }
    }

    public interface Renderable {

        int getStatus();

        String getContent();

        String getPageTitle();
    }

    public static void main(String[] args) throws Exception {
        final LogEntryManager logs = new LogEntryManager();

        RatpackServer.start(server -> server
            .handlers(chain -> chain
                .get(":id", ctx -> {
                    Optional<LogEntry> log = logs.get(ctx.getPathTokens().get("id"));
                    if (log.isPresent()) {
                        ctx.insert(Registry.single(LogEntry.class, log.get()), new LogEntryRenderer(), new Renderer());
                    } else {
                        ctx.insert(new NotFound(), new Renderer());
                    }
                })
            )
        );
    }
}
