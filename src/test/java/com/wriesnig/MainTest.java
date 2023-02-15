package com.wriesnig;

import com.wriesnig.api.git.GitApi;
import com.wriesnig.db.expertise.ExpertiseDatabase;
import com.wriesnig.db.stack.StackDatabase;
import com.wriesnig.db.stack.sodumpsconvert.ConvertApplication;
import com.wriesnig.utils.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @BeforeAll
    public static void setUp(){
        Logger.deactivatePrinting();
    }

    @Test
    public void isConfigPropertiesSet(){
        try (MockedConstruction<CharacterizerApplication> mocked = mockConstruction(CharacterizerApplication.class,
                (mock, context) -> {
                    doNothing().when(mock).run();
                })) {
            Main.main(new String[]{"src/main/resources/src/testConfig.properties"});
            CharacterizerApplication app = mocked.constructed().get(0);
            verify(app, times(1)).run();
            assertEquals("testToken", GitApi.getToken());
            assertTrue(StackDatabase.isCredentialsSet());
            assertTrue(ExpertiseDatabase.isCredentialsSet());
        }
    }

    @Test
    public void convertApplicationCalled(){
        try (MockedConstruction<ConvertApplication> mocked = mockConstruction(ConvertApplication.class,
                (mock, context) -> {
                    doNothing().when(mock).run();
                })) {
            Main.main(new String[]{"c"});
            ConvertApplication app = mocked.constructed().get(0);
            verify(app, times(1)).run();
        }
    }

    @Test
    public void wrongArgumentsCount(){
        try (MockedConstruction<CharacterizerApplication> mocked = mockConstruction(CharacterizerApplication.class,
                (mock, context) -> {
                    doNothing().when(mock).run();
                })) {
            Main.main(new String[]{});
            boolean isAppConstructed = mocked.constructed().size() != 0;
            assertFalse(isAppConstructed);
        }
    }




}
