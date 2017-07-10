package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by RS045609 on 7/7/2017.
 */
public class ManageResourceRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the resource tab$")
    public void goToConfigurationTab() {
        jwalaUi.clickTab("Resources");
    }

    @And("I expanded group")
    public void expandGroup() {
        jwalaUi.clickTreeItemExpandCollapseIcon("Rahul group");
    }

    @And("I expanded webservers")
    public void expandWebserver() {
        jwalaUi.clickTreeItemExpandCollapseIcon("Web Servers");
    }

    @And("I clicked on webserver")
    public void clickWebserver() {
        jwalaUi.clickComponentForUpload("Rahul webserver");
    }

    @And("I clicked on add resource")
    public void addResource() {
        jwalaUi.clickAddResource();
    }

    @And("^I fill in the \"Deploy Name\" field with \"(.*)\"$")
    public void setDeployName(String deployFilename) {
        jwalaUi.sendKeys(By.name("deployFilename"), deployFilename);
    }

    @And("^I fill in the \"Deploy Path\" field with \"(.*)\"$")
    public void setDeployPath(String deployPath) {
        jwalaUi.sendKeys(By.xpath("//label[text()='Deploy Path']/following-sibling::input"), deployPath);
        jwalaUi.sleep();
    }

    @And("^I choose the resource file \"(.*)\"$")
    public void selectMediaArchiveFile(final String archiveFileName) {
        final Path mediaPath = Paths.get(jwalaUi.getProperties().getProperty("file.upload.dir") + "/" + archiveFileName);
        jwalaUi.sendKeys(By.name("templateFile"), mediaPath.normalize().toString());
    }

    @And("^I click the upload resource dialog ok button$")
    public void clickAddMediaOkDialogBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }

    @Then("check resource uploaded successful")
    public void checkForResource() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//input[contains(@class, 'noSelect')]/following-sibling::span"));
    }

}