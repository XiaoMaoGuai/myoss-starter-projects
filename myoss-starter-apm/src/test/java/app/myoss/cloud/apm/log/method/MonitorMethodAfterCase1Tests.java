/*
 * Copyright 2018-2019 https://github.com/myoss
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
 *
 */

package app.myoss.cloud.apm.log.method;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import app.myoss.cloud.apm.log.method.aspectj.MonitorMethodAfter;
import app.myoss.cloud.apm.log.method.aspectj.MonitorMethodAround;
import app.myoss.cloud.apm.log.method.aspectj.MonitorMethodBefore;
import app.myoss.cloud.apm.log.method.aspectj.annotation.EnableAopLogMethod;
import app.myoss.cloud.apm.log.method.aspectj.annotation.LogMethodAfter;

/**
 * 注解 {@link LogMethodAfter} 放在方法上
 *
 * @author Jerry.Chen
 * @since 2019年1月30日 下午3:31:58
 */
@SpringBootTest(properties = { "myoss-cloud.log.method.app-name:myoss-starter-apm" })
@RunWith(SpringRunner.class)
public class MonitorMethodAfterCase1Tests {
    @Rule
    public OutputCapture       output = new OutputCapture();
    @Rule
    public ExpectedException   thrown = ExpectedException.none();

    @Autowired
    private ApplicationContext context;

    @Test
    public void isInjectMonitorMethodAdvice() {
        context.getBean(MonitorMethodBefore.class);
        context.getBean(MonitorMethodAfter.class);
        context.getBean(MonitorMethodAround.class);
    }

    @Autowired
    private LogOnMethodTest logOnMethodTest;

    @Test
    public void logOnMethodMatchTest1() {
        logOnMethodTest.isMatch();
        long endTimeMillis = System.currentTimeMillis();
        String printLog = this.output.toString();
        assertThat(printLog).contains(
                "[app.myoss.cloud.apm.log.method.MonitorMethodAfterCase1Tests$LogOnMethodTest#isMatch]",
                "[MonitorMethodAfter.java");

        String json = StringUtils.substring(printLog, printLog.indexOf(" - {") + 3);
        JSONObject jsonAfter = JSON.parseObject(json);
        assertThat(jsonAfter.getLong("end")).isLessThanOrEqualTo(endTimeMillis);
        assertThat(jsonAfter.getString("result")).isEqualTo("matched");
        assertThat(jsonAfter.getString("app")).isEqualTo("myoss-starter-apm");
    }

    @Test
    public void logOnMethodMatchTest2() {
        String name = "jerry";
        logOnMethodTest.isMatch2(name);
        long endTimeMillis = System.currentTimeMillis();
        String printLog = this.output.toString();
        assertThat(printLog).contains(
                "[app.myoss.cloud.apm.log.method.MonitorMethodAfterCase1Tests$LogOnMethodTest#isMatch2]",
                "[MonitorMethodAfter.java");

        String json = StringUtils.substring(printLog, printLog.indexOf(" - {") + 3);
        JSONObject jsonAfter = JSON.parseObject(json);
        assertThat(jsonAfter.getLong("end")).isLessThanOrEqualTo(endTimeMillis);
        assertThat(jsonAfter.getString("result")).isEqualTo("matched2, " + name);
        assertThat(jsonAfter.getString("app")).isEqualTo("myoss-starter-apm");
    }

    @Test
    public void logOnMethodIsNotMatchTest() {
        logOnMethodTest.isNotMatch();
        String printLog = this.output.toString();
        assertThat(printLog).isEmpty();
    }

    // 开启AspectJ
    @EnableAspectJAutoProxy
    @EnableAopLogMethod
    @Configuration
    protected static class Config {
        @Bean
        public LogOnMethodTest logOnMethodTest() {
            return new LogOnMethodTest();
        }
    }

    /**
     * 注解 {@link LogMethodAfter} 放在方法上
     */
    protected static class LogOnMethodTest {
        @LogMethodAfter
        public String isMatch() {
            return "matched";
        }

        @LogMethodAfter
        public String isMatch2(String name) {
            return "matched2, " + name;
        }

        public String isNotMatch() {
            return "not matched";
        }
    }
}
