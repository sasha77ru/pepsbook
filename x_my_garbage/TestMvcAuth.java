package ru.sasha77.spring.pepsbook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TestMvcAuth {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    //It work wo spring-security-test dependency
    @Test
    public void test1() throws Exception {
        mockMvc.perform(get("/rest/allUsersSimple").header(HttpHeaders.AUTHORIZATION,
                "Basic " + Base64Utils.encodeToString("buzz:infinity".getBytes())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(it->System.out.println(it.getResponse().getContentAsString()));
    }

    //it works with spring-security-test dependency
    @Test
    public void test2() throws Exception {
        mockMvc.perform(get("/rest/getUser").with(httpBasic("aaa","aaa")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(it->System.out.println(it.getResponse().getContentAsString()));
    }
}