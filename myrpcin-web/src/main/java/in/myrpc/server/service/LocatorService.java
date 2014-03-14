/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.server.model.Locator;
import java.util.Map;

/**
 * Service methods for deriving keys from locators and vise verse
 *
 * @author kguthrie
 */
public interface LocatorService {

    <T> String createLocator(T entity);

    <T> String createLocator(T entity, Map<String, String> headers);

    Locator openLocator(String locatorString);

}
