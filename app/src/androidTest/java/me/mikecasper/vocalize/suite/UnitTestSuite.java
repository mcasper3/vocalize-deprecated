package me.mikecasper.vocalize.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import me.mikecasper.vocalize.LogInActivityTest;
import me.mikecasper.vocalize.models.ModelTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LogInActivityTest.class,
        ModelTest.class})
public class UnitTestSuite {}
