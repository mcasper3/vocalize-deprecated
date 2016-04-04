package me.mikecasper.musicvoice.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import me.mikecasper.musicvoice.LogInActivityTest;
import me.mikecasper.musicvoice.models.ModelTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LogInActivityTest.class,
        ModelTest.class})
public class UnitTestSuite {}
