
package com.capstone.server.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.capstone.server.controller.RestUriConstants;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants;

/**
 * This LoginInterceptor will block pages that only administrator can access.
 * Everything different than that will be allowed to access without the
 * necessity to have an user logged in.
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // remove the project name from the URI
        String controller = uri.substring(contextPath.length());

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Constants.SESSION_USER);

        if (user == null && isRestrictToLoggedUser(controller)) {
            session.setAttribute(Constants.IS_REDIRECT, true);
            response.sendRedirect(contextPath + "/");
            return false;
        }

        return true;
    }

    private boolean isRestrictToLoggedUser(String controller) {
        return controller.startsWith(RestUriConstants.DEVICE_CONTROLLER + "/" + RestUriConstants.VIEW) ||
               controller.startsWith(RestUriConstants.QUESTION_CONTROLLER + "/" + RestUriConstants.REGISTER) ||
               controller.startsWith(RestUriConstants.QUESTION_CONTROLLER + "/" + RestUriConstants.VIEW);
    }
}
