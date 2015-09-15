
package com.capstone.server.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.capstone.server.controller.RestUriConstants;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        return true;
        /*String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // remove the project name from the URI
        String controller = uri.substring(contextPath.length());

        if (controller.startsWith(RestUriConstants.LOGIN_CONTROLLER) ||
                uri.contains("resources")) {
            return true;
        }

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if(user != null) {
            return true;
        }

        response.sendRedirect(contextPath + RestUriConstants.LOGIN_CONTROLLER
                + "/" + RestUriConstants.REGISTER);
        return false;*/
    }
}
