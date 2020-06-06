package main.java;

import javax.servlet.*;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        boolean skip = false;
        try {
            skip = Boolean.parseBoolean(httpRequest.getParameter("skip"));
        } catch(Exception e) {
            skip = false;
        }

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        if (true) {
            chain.doFilter(request, response);
            return;
        }

        if (skip || httpRequest.getRequestURI().endsWith(".png") || this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            System.out.println("Filter Passed: " + httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        if (httpRequest.getSession(true).getAttribute("user") == null) httpResponse.sendRedirect("login.html");
        else chain.doFilter(request, response);
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        if (requestURI.endsWith("/")) return false;
        for (String uri : allowedURIs) {
            System.out.println("Req: " + requestURI + " ~ URI: " + uri);
            if(requestURI.toLowerCase().endsWith(uri)) return true;
        }
        return false;
        //return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("styles.css");
        allowedURIs.add("resources/fabflixlogo.png");
        allowedURIs.add("fabflixlogo.png");
        allowedURIs.add("resources/login.html");
        allowedURIs.add("favicon.ico");
        allowedURIs.add("favicon.png");
        allowedURIs.add("resources/favicon.ico");
    }

    public void destroy() {

    }
}
