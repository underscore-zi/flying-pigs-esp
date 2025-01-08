package org.little_canada.mc.flyingpigs;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FlyingPigESP extends Module {
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final SettingGroup sgTracers = this.settings.createGroup("Tracers");

    // ESP Settings
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    private final Setting<SettingColor> boxColor = sgRender.add(new ColorSetting.Builder()
        .name("box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    // Tracer Settings
    private final Setting<Boolean> tracersEnabled = sgTracers.add(new BoolSetting.Builder()
        .name("tracers-enabled")
        .description("Enables tracer lines to flying mobs.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgTracers.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color of tracer lines.")
        .defaultValue(new SettingColor(255, 255, 0, 150))
        .build()
    );

    private ConcurrentLinkedQueue<Integer> foundMobs = new ConcurrentLinkedQueue<Integer>();
    private int count;
    private final int maxFoundMobs = 128;

    public FlyingPigESP() {
        super(AddonTemplate.CATEGORY, "flying-pigs-esp", "ESP and Tracers for Mobs wearing elytra");
    }

    @EventHandler
    private void onRender3d(Render3DEvent event) {
        count = 0;
        for (Entity entity : mc.world.getEntities()) {
            if(entity instanceof MobEntity mob) {
                mob.getArmorItems().forEach((item) -> {
                    if (item.getItem().toString().equals("minecraft:elytra")) {
                        // ESP Rendering
                        double x = net.minecraft.util.math.MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
                        double y = net.minecraft.util.math.MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
                        double z = net.minecraft.util.math.MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

                        Box box = entity.getBoundingBox();
                        event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, boxColor.get(), lineColor.get(), ShapeMode.Both, 0);

                        // Tracer Rendering
                        if (tracersEnabled.get()) {
                            double entityX = entity.prevX + (entity.getX() - entity.prevX) * event.tickDelta;
                            double entityY = entity.prevY + (entity.getY() - entity.prevY) * event.tickDelta;
                            double entityZ = entity.prevZ + (entity.getZ() - entity.prevZ) * event.tickDelta;

                            // Draw tracer line from center of screen to entity
                            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                                entityX, entityY + entity.getHeight() / 2, entityZ,
                                tracerColor.get());
                        }

                        count++;
                        addDiscovery(mob);
                    }
                });
            }
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    private synchronized void addDiscovery(MobEntity mob) {
        var id = mob.getId();
        if (!foundMobs.contains(id)) {
            foundMobs.add(id);

            var coords = ChatUtils.formatCoords(mob.getPos());

            MutableText text = Text.literal("Found a flying ")
                .append(Text.of(mob.getName().getString()))
                .append(Text.literal(" at "))
                .append(Text.of(coords));

            ChatUtils.sendMsg("FlyingPigESP", text);

            if (foundMobs.size() > maxFoundMobs) {
                foundMobs.poll();
            }
        }
    }
}
