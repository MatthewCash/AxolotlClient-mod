/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.freelook;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.FeatureDisabler;
import net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;

public class Freelook extends AbstractModule {

	private static final Freelook INSTANCE = new Freelook();
	private static final KeyBinding KEY = new KeyBinding("key.freelook", Keyboard.KEY_V,
		"category.axolotlclient");
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final OptionCategory category = new OptionCategory("freelook");
	private final EnumOption mode = new EnumOption("mode",
		value -> FeatureDisabler.update(),
		new String[]{"snap_perspective", "freelook"},
		"freelook");
	private final EnumOption perspective = new EnumOption("perspective", Perspective.values(),
		Perspective.THIRD_PERSON_BACK.toString());
	private final BooleanOption invert = new BooleanOption("invert", false);
	private final BooleanOption toggle = new BooleanOption("toggle", false);
	private float yaw, pitch;
	private boolean active;
	private int previousPerspective;

	public static Freelook getInstance() {
		return INSTANCE;
	}

	@Override
	public void init() {
		KeyBindingHelper.registerKeyBinding(KEY);
		category.add(enabled, mode, perspective, invert, toggle);
		AxolotlClient.CONFIG.addCategory(category);
	}

	@Override
	public void tick() {
		if (!enabled.get())
			return;

		if (toggle.get()) {
			if (KEY.wasPressed()) {
				if (active) {
					stop();
				} else {
					start();
				}
			}
		} else {
			if (KEY.isPressed()) {
				if (!active) {
					start();
				}
			} else if (active) {
				stop();
			}
		}
	}

	private void stop() {
		active = false;
		client.worldRenderer.scheduleTerrainUpdate();
		client.options.perspective = previousPerspective;
	}

	private void start() {
		active = true;

		previousPerspective = client.options.perspective;
		client.options.perspective = Perspective.valueOf(perspective.get()).ordinal();

		Entity camera = client.getCameraEntity();

		if (camera == null)
			camera = client.player;
		if (camera == null)
			return;

		yaw = camera.yaw;
		pitch = camera.pitch;
	}

	public boolean consumeRotation(float dx, float dy) {
		if (!active || !enabled.get() || !mode.get().equals("freelook"))
			return false;

		if (!invert.get())
			dy = -dy;

		yaw += dx * 0.15F;
		pitch += dy * 0.15F;

		if (pitch > 90) {
			pitch = 90;
		} else if (pitch < -90) {
			pitch = -90;
		}

		client.worldRenderer.scheduleTerrainUpdate();
		return true;
	}

	public float yaw(float defaultValue) {
		if (!active || !enabled.get() || !mode.get().equals("freelook"))
			return defaultValue;

		return yaw;
	}

	public float pitch(float defaultValue) {
		if (!active || !enabled.get() || !mode.get().equals("freelook"))
			return defaultValue;

		return pitch;
	}

	public boolean needsDisabling() {
		return false;
	}
}
