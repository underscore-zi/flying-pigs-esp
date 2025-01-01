package org.little_canada.mc.flyingpigs;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("__zi");

    @Override
    public void onInitialize() {
        Modules.get().add(new FlyingPigESP());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "org.little_canada.mc.flyingpigs.FlyingPigESP";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("underscore-zi", "flying-pigs-esp");
    }
}
