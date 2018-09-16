package org.littlewings.clover;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.DefaultClassIntrospector;
import org.jboss.logging.Logger;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.servlet.ResteasyServletInitializer;
import org.jboss.weld.environment.servlet.Listener;
import org.littlewings.clover.config.CrawlConfig;
import org.littlewings.clover.config.ScheduledListener;
import org.reflections.Reflections;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class StandaloneServer implements AutoCloseable {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    Logger logger = Logger.getLogger(StandaloneServer.class);

    String bindAddress;
    int port;

    Undertow undertow;

    public StandaloneServer(String bindAddress, int port) {
        this.bindAddress = bindAddress;
        this.port = port;
    }

    public StandaloneServer() {
        this("0.0.0.0", 8080);
    }

    public static void main(String... args) {
        StandaloneServer server = new StandaloneServer();
        server.start();
    }

    public void start() {
        long startTime = System.nanoTime();
        logger.infof("initialize server...");

        Set<Class<?>> jaxrsClasses = new HashSet<>();

        Reflections applications = new Reflections(StandaloneServer.class.getPackage().getName());
        jaxrsClasses.addAll(applications.getSubTypesOf(Application.class));
        jaxrsClasses.addAll(applications.getTypesAnnotatedWith(Path.class));
        jaxrsClasses.addAll(applications.getTypesAnnotatedWith(Provider.class));

        DeploymentInfo deploymentInfo;

        try {
            deploymentInfo =
                    Servlets
                            .deployment()
                            .setClassLoader(StandaloneServer.class.getClassLoader())
                            .setContextPath("/")
                            .setDeploymentName("clover-search")
                            .addWelcomePage("index.html")
                            .setResourceManager(retrieveResourceManager())
                            .addInitParameter("resteasy.injector.factory", CdiInjectorFactory.class.getName())
                            .addServletContainerInitializer(
                                    new ServletContainerInitializerInfo(ResteasyServletInitializer.class,
                                            DefaultClassIntrospector.INSTANCE.createInstanceFactory(ResteasyServletInitializer.class),
                                            jaxrsClasses)
                            )
                            .addListener(Servlets.listener(Listener.class))
                            .addListener(Servlets.listener(ScheduledListener.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        deploymentManager.deploy();

        HttpHandler httpHandler;
        try {
            httpHandler =
                    Handlers
                            .path(deploymentManager.start());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }

        undertow =
                Undertow
                        .builder()
                        .addHttpListener(port, bindAddress)
                        .setHandler(httpHandler)
                        .build();

        undertow.start();

        long startupTime = System.nanoTime() - startTime;
        logger.infof("server startup, %d msec", TimeUnit.MILLISECONDS.convert(startupTime, TimeUnit.NANOSECONDS));
    }

    @Override
    public void close() throws Exception {
        undertow.stop();
    }

    ResourceManager retrieveResourceManager() {
        String strategy = CrawlConfig.retrieveResourceManagerStrategy();
        ResourceManager resourceManager;

        switch (strategy) {
            case "classpath":
                resourceManager = new ClassPathResourceManager(StandaloneServer.class.getClassLoader(), "static");
                break;
            case "path":
                try {
                    java.nio.file.Path current =
                            Paths
                                    .get(StandaloneServer.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                                    .getParent()
                                    .getParent();
                    resourceManager = new PathResourceManager(current.resolve("src/main/resources/static"));
                    break;
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            default:
                throw new IllegalArgumentException("Unknown resource-manager strategy = " + strategy);
        }

        logger.infof("selected resource-manager = %s", resourceManager.getClass().getName());

        return resourceManager;
    }
}
