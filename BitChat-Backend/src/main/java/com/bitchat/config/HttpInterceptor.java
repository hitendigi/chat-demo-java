package com.bitchat.config;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.bitchat.model.Session;
import com.bitchat.model.User;
import com.bitchat.repository.SessionRepository;
import com.bitchat.repository.UserRepository;
import com.bitchat.util.Utils;

@Component
public class HttpInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Utils utils;

    
    public HttpInterceptor() {

    }

    private Properties getPropertiesFromCookies(Cookie cookie[]) {
        if (cookie == null) {
            return null;
        }
        Properties cookies = new Properties();
        for (Cookie c : cookie) {
            cookies.put(c.getName(), c.getValue());
        }
        return cookies;
    }

    public void loginWithCookies(Properties cookies, Session session) {
        if (cookies != null) {
            if (cookies.containsKey("username") && cookies.containsKey("password")) {
                User dbUser = userRepository.findByUsername("" + cookies.get("username"));
                if ((dbUser != null) && dbUser.getPassword().equals(cookies.get("password"))) {
                    session.setUser(dbUser);
                }
            }
        }
    }

    private void loginWithBasicAuth(HttpServletRequest request, HttpSession httpSession, Session session) throws UnsupportedEncodingException {
        String auth = request.getHeader("Authorization");
        if (auth != null) {
            String[] split = auth.split(" ");
            if ("basic".equals(split[0].toLowerCase())) {
                String base64 = split[1];
                String cred = new String(Base64.getDecoder().decode(base64), "UTF-8");
                String username = cred.substring(0, cred.indexOf(":"));
                String password = cred.substring(cred.indexOf(":") + 1, cred.length());
                User dbUser = userRepository.findByUsername(username);
                if ((dbUser != null) && dbUser.getPassword().equals(utils.hash(password))) {
                    session.setUser(dbUser);
                }
            }
        }
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 3600000)
    public void clearExpiredSessions() {
        List<Session> expired = sessionRepository.findExpired(System.currentTimeMillis() - 3600000);
        sessionRepository.deleteAll(expired);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession httpSession = request.getSession();

        Session session = sessionRepository.findById(httpSession.getId()).orElse(null);
        if (session == null) {
            session = new Session(httpSession.getId(), null, System.currentTimeMillis());
        }
        session.setLastModified(System.currentTimeMillis());
        sessionRepository.save(session);

        loginWithBasicAuth(request, httpSession, session);
        loginWithCookies(getPropertiesFromCookies(request.getCookies()), session);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception {
    }
}
