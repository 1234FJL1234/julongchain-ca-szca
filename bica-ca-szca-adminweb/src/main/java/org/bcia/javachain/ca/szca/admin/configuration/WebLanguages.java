
/*
 * Copyright ? 2018  深圳市电子商务安全证书管理有限公司(SZCA,深圳CA) 版权所有
 * Copyright ? 2018  SZCA. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bcia.javachain.ca.szca.admin.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.bcia.javachain.ca.szca.admin.common.LanguageProperties;
import org.ejbca.config.GlobalConfiguration;

import org.bcia.javachain.ca.szca.common.bcca.core.model.InternalEjbcaResources;



public class WebLanguages implements java.io.Serializable {
    private static final long serialVersionUID = -2381623760140383128L;

    private static final Logger log = Logger.getLogger(WebLanguages.class);

    /** Internal localization of logs and errors */
    private static final InternalEjbcaResources intres = InternalEjbcaResources.getInstance();

    /** Constructor used to load static content. An instance must be declared with this constructor before
     *  any WebLanguage object can be used. */
    /** Special constructor used by Ejbca web bean */
    private void init(ServletContext servletContext, GlobalConfiguration globalconfiguration) {
        if(languages == null){
            // Get available languages.
            availablelanguages=null;
            String availablelanguagesstring = globalconfiguration.getAvailableLanguagesAsString();
            availablelanguages =  availablelanguagesstring.split(",");
            for(int i=0; i < availablelanguages.length;i++){
                availablelanguages[i] = availablelanguages[i].trim().toLowerCase();
                if (availablelanguages[i].equalsIgnoreCase("se")) {  /* For compatibility with EJBCA 6.2.x and before */
                    availablelanguages[i] = "sv";
                }
                if (availablelanguages[i].equalsIgnoreCase("ua")) {  /* For compatibility with EJBCA 6.2.x and before */
                    availablelanguages[i] = "uk";
                }
            }
            // Load available languages
            languages = new LanguageProperties[availablelanguages.length];
            for(int i = 0; i < availablelanguages.length; i++){
                languages[i] = new LanguageProperties();
                String propsfile = "/" + globalconfiguration.getLanguagePath() + "/"
                + globalconfiguration.getLanguageFilename() + "."
                + availablelanguages[i] +".properties";
                
                InputStream is = null;
                if (servletContext != null) {
                	is = servletContext.getResourceAsStream(propsfile);
                } else {
                    is = this.getClass().getResourceAsStream(propsfile);                	
                }
                try {
                    if (is == null) {
                        //if not available as stream, try it as a file
                        is = new FileInputStream("/tmp" + propsfile);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Loading language from file: " + propsfile);
                    }
                    languages[i].load(is);
                } catch (IOException e) {
                    throw new IllegalStateException("Properties file " + propsfile + " could not be read.", e);
                }
               
            }
            // Get languages English and native names
            languagesenglishnames = new String[availablelanguages.length];
            languagesnativenames = new String[availablelanguages.length];
            for(int i = 0; i < availablelanguages.length; i++){
                languagesenglishnames[i] = languages[i].getProperty("LANGUAGE_ENGLISHNAME");
                languagesnativenames[i] = languages[i].getProperty("LANGUAGE_NATIVENAME");
            }
        }
    }

    public WebLanguages(ServletContext servletContext, GlobalConfiguration globalconfiguration, int preferedlang, int secondarylang) {
        init(servletContext, globalconfiguration);
        this.userspreferedlanguage=preferedlang;
        this.userssecondarylanguage=secondarylang;
    }


    /** The main method that looks up the template text in the users preferred language. */
    public  String getText(String template, Object... params){
      String returnvalue = null;
      try{
        returnvalue= languages[userspreferedlanguage].getMessage(template, params);
        if(returnvalue == null){
          returnvalue= languages[userssecondarylanguage].getMessage(template, params);
        }
        if(returnvalue == null){
            returnvalue= intres.getLocalizedMessage(template, params);
        }        
      }catch(java.lang.NullPointerException e){}
      if(returnvalue == null) {
        returnvalue = template;
      }
      return returnvalue;
    }

    /* Returns a text string array containing the available languages */
    public String[] getAvailableLanguages(){
      return availablelanguages;
    }

    /* Returns a text string array containing the languages English names */
    public String[] getLanguagesEnglishNames(){
      return languagesenglishnames;
    }

    /* Returns a text string array containing the languages native names */
    public String[] getLanguagesNativeNames(){
      return languagesnativenames;
    }


    // Protected fields
    private int userspreferedlanguage;
    private int userssecondarylanguage;

    private String[] availablelanguages;
    private String[] languagesenglishnames;
    private String[] languagesnativenames;
    private LanguageProperties[] languages = null;

}
