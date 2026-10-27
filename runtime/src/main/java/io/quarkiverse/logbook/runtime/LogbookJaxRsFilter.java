package io.quarkiverse.logbook.runtime;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import org.zalando.logbook.Logbook;
import org.zalando.logbook.jaxrs.LogbookServerFilter;

@ApplicationScoped
public class LogbookJaxRsFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

    private final LogbookServerFilter delegate;

    @Inject
    public LogbookJaxRsFilter(Logbook logbook) {
        this.delegate = new LogbookServerFilter(logbook);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        delegate.filter(requestContext);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        delegate.filter(requestContext, responseContext);
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        delegate.aroundWriteTo(context);
    }
}
