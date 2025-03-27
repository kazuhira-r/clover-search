package org.littlewings.clover;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.DefaultClassIntrospector;
import jakarta.servlet.ServletException;
import java.util.Set;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.servlet.ResteasyServletInitializer;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.littlewings.clover.rest.RestApplication;

public class Server {
    public static void main(String... args) throws ServletException, NoSuchMethodException {
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;

        DeploymentInfo deploymentInfo =
                Servlets
                        .deployment()
                        .setClassLoader(Server.class.getClassLoader())
                        .setContextPath("/")
                        .setDeploymentName("clover-search")
                        .addInitParameter("resteasy.injector.factory", CdiInjectorFactory.class.getName())
                        .addServletContainerInitializer(
                                new ServletContainerInitializerInfo(EnhancedListener.class,
                                        DefaultClassIntrospector.INSTANCE.createInstanceFactory(EnhancedListener.class),
                                        null
                                )
                        )
                        .addServletContainerInitializer(
                                new ServletContainerInitializerInfo(ResteasyServletInitializer.class,
                                        DefaultClassIntrospector.INSTANCE.createInstanceFactory(ResteasyServletInitializer.class),
                                        Set.of(RestApplication.class)
                                )
                        );

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        deploymentManager.deploy();

        ResourceManager indexResourceManager = new ClassPathResourceManager(Server.class.getClassLoader(), "static");
        ResourceManager jsResourceManager = new ClassPathResourceManager(Server.class.getClassLoader(), "static/js");
        ResourceManager cssResourceManager = new ClassPathResourceManager(Server.class.getClassLoader(), "static/css");

        HttpHandler applicationHandler = deploymentManager.start();

        HttpHandler handler =
                Handlers
                        .path(Handlers.redirect("/"))
                        .addPrefixPath("/js", Handlers.resource(jsResourceManager))
                        .addPrefixPath("/css", Handlers.resource(cssResourceManager))
                        .addPrefixPath("/", exchange -> {
                            if (exchange.getRelativePath().equals("/")) {
                                exchange.setRelativePath("/index.html");
                                exchange.setRequestPath("/index.html");
                                exchange.dispatch(Handlers.resource(indexResourceManager));
                            } else {
                                exchange.dispatch(applicationHandler);
                            }
                        });

        Undertow undertow =
                Undertow
                        .builder()
                        .addHttpListener(port, "0.0.0.0")
                        .setHandler(handler)
                        .build();

        undertow.start();
    }
}
