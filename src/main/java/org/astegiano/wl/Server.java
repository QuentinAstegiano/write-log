package org.astegiano.wl;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.astegiano.wl.api.LogEntryManager;
import org.astegiano.wl.data.LogEntry;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.registry.Registry;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
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

    public static class FileLogEntryRenderer implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            File file = ctx.file("entry_1.html").toFile();
            String content = Joiner.on("").join(Files.readLines(file, Charset.forName("utf-8")));

            ctx.next(Registry.single(Renderable.class, new Renderable() {
                @Override
                public int getStatus() {
                    return 200;
                }

                @Override
                public String getContent() {
                    return content;
                }

                @Override
                public String getPageTitle() {
                    return "No title... yet";
                }
            }));
        }
    }

    public static class Renderer implements Handler {
        @Override
        public void handle(Context ctx) throws Exception {
            File file = ctx.file("index.html").toFile();

            BufferedReader reader = Files.newReader(file, Charset.forName("UTF-8"));
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(reader, "template.mustache");

            StringWriter writer = new StringWriter();

            Renderable renderable = ctx.get(Renderable.class);
            mustache.execute(writer, renderable);

            ctx.getResponse()
                .contentType("text/html;charset=utf-8")
                .status(renderable.getStatus())
                .send(writer.toString());
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
            .serverConfig( c -> c.baseDir(BaseDir.find("assets/index.html")))
            .handlers(chain -> chain
                            .get("entry", ctx -> {
                                ctx.insert(new FileLogEntryRenderer(), new Renderer());
                            })
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
