/*
 * Copyright 2019 Christophe Marchand
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.marchand.oxygen.maven.project.support.maven;

import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.InvokerLogger;

/**
 *
 * @author cmarchand
 */
public class Log4JLoggerWrapper implements InvokerLogger {
    private final Logger logger;
    
    public Log4JLoggerWrapper(Logger logger) {
        super();
        this.logger=logger;
    }

    @Override
    public void debug(String string) {
        logger.debug(string);
    }
    @Override
    public void debug(String string, Throwable thrwbl) {
        logger.debug(string,thrwbl);
    }
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    @Override
    public void info(String string) {
        logger.info(string);
    }
    @Override
    public void info(String string, Throwable thrwbl) {
        logger.info(string, thrwbl);
    }
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }
    @Override
    public void warn(String string) {
        logger.warn(string);
    }
    @Override
    public void warn(String string, Throwable thrwbl) {
        logger.warn(string,thrwbl);
    }
    @Override
    public boolean isWarnEnabled() {
        return true;
    }
    @Override
    public void error(String string) {
        logger.error(string);
    }
    @Override
    public void error(String string, Throwable thrwbl) {
        logger.error(string,thrwbl);
    }
    @Override
    public boolean isErrorEnabled() {
        return true;
    }
    @Override
    public void fatalError(String string) {
        logger.fatal(string);
    }
    @Override
    public void fatalError(String string, Throwable thrwbl) {
        logger.fatal(string,thrwbl);
    }
    @Override
    public boolean isFatalErrorEnabled() {
        return true;
    }

    @Override
    public void setThreshold(int i) {}

    @Override
    public int getThreshold() {
        return 1;
    }
    
}
