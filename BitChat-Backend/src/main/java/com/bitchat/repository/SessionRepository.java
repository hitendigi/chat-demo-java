package com.bitchat.repository;

import org.springframework.stereotype.Component;

import com.bitchat.model.Session;
import com.bitchat.model.User;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author masoud
 */
@Component
public class SessionRepository {

    private final Map<String, Session> map = new HashMap<>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void deleteById(String id) {
        try {
            lock.writeLock().lock();
            id = id.toLowerCase();
            map.remove(id);
        } finally {
            lock.writeLock().unlock();
        }

    }
    
    public List<Session> findAll(){
    	List<Session> sessionList = new ArrayList<Session>();
    	for (String id : map.keySet()) {
           sessionList.add(map.get(id));
        }
    	return sessionList;
    }

    public void save(Session session) {
        try {
            lock.writeLock().lock();
            session.setId(session.getId().toLowerCase());
            map.put(session.getId(), session);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Session> findById(String id) {
        try {
            lock.readLock().lock();
            id = id.toLowerCase();
            if (map.containsKey(id)) {
                return Optional.of(map.get(id));
            } else {
                return Optional.empty();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void deleteAll(List<Session> list) {
        try {
            lock.writeLock().lock();
            list.stream().forEach(x -> map.remove(x.getId()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Session> findExpired(long l) {
        try {
            lock.readLock().lock();
            List<Session> list = new ArrayList<>();
            for (String id : map.keySet()) {
                if (map.get(id).getLastModified() < l) {
                    list.add(map.get(id));
                }
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Session> findByUsername(String username) {
        try {
            lock.readLock().lock();
            List<Session> list = new ArrayList<>();
            for (String id : map.keySet()) {
                User user = map.get(id).getUser();
                if ((user != null) && (user.getUsername().equals(username))) {
                    list.add(map.get(id));
                }
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }
}
