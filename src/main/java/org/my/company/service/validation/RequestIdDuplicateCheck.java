package org.my.company.service.validation;


import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It should use DB and verify each id on request and not allowed to resubmit the same id for the second time.
 */
@Service
public class RequestIdDuplicateCheck {

    //our in-memory db
    private final Set<String> uuids = ConcurrentHashMap.newKeySet();

    public void registerNewRequestOrThrow(String uuid) throws DuplicateRequestException {
        if (!uuids.add(uuid)) {
            throw new DuplicateRequestException(uuid + " was already requested.");
        }
    }
}
