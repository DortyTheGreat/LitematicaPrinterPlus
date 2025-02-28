package com.LitematicaPrinterPlus.addon.modules;


import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.render.Camera;



import com.LitematicaPrinterPlus.addon.Reflector; 
import java.util.Arrays;
import java.lang.reflect.Method;

public class SecondPerspective extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> staticView = sgGeneral.add(new BoolSetting.Builder()
        .name("static")
        .description("Disables settings that move the view.")
        .defaultValue(true)
        .build()
    );

    public Vec3d pos = new Vec3d(0, 0, 0);

    private Perspective perspective;
    private double speedValue;

    public float yaw, pitch;
    public float prevYaw, prevPitch;

    private double fovScale;
    private boolean bobView;

    private boolean forward, backward, right, left, up, down;

    public SecondPerspective(Category cat) {
		super(cat, "second-perspective", "turns camera on second perspective mode");
	}

    @Override
    public void onActivate() {
        fovScale = mc.options.getFovEffectScale().getValue();
        bobView = mc.options.getBobView().getValue();
        if (staticView.get()) {
            mc.options.getFovEffectScale().setValue((double)0);
            mc.options.getBobView().setValue(false);
        }
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        perspective = mc.options.getPerspective();
        

        pos = mc.gameRenderer.getCamera().getPos();

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            yaw += 180;
            pitch *= -1;
        }

        
    }

    @Override
    public void onDeactivate() {
        
        mc.options.setPerspective(perspective);
        if (staticView.get()) {
            mc.options.getFovEffectScale().setValue(fovScale);
            mc.options.getBobView().setValue(bobView);
        }
    }

    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.cameraEntity.isInsideWall()) mc.getCameraEntity().noClip = true;
        if (!perspective.isFirstPerson()) mc.options.setPerspective(Perspective.FIRST_PERSON);
		
		/// method_19322
		
		Method privateMethod_ = null;
		
		for (Method method : mc.gameRenderer.getCamera().getClass().getDeclaredMethods()) {
			info(method.getName());
			if ( method.getName() == "method_19322"){
				info("found method");
				privateMethod_ = method;
			}
		}
		
		
		try{
			if (privateMethod_ != null){
				// Делаем метод доступным
				privateMethod_.setAccessible(true);
				
				// Вызываем приватный метод
				privateMethod_.invoke(mc.gameRenderer.getCamera(), pos);
			}
		}catch(Exception e){
			info(Arrays.toString(e.getStackTrace()));
		}
		
		try{
			
			Camera cam = mc.gameRenderer.getCamera();
			
			// Получаем приватный метод через рефлексию
			Method privateMethod = cam.getClass().getDeclaredMethod("method_19322");
			
			// Делаем метод доступным
			privateMethod.setAccessible(true);
			
			// Вызываем приватный метод
			privateMethod.invoke(cam, pos);
		}catch(Exception e){
			info(Arrays.toString(e.getStackTrace()));
		}
		
		try{
			Reflector.invokePrivateMethod(mc.gameRenderer.getCamera(), "method_19322", pos); // setPos
			Reflector.invokePrivateMethod(mc.gameRenderer.getCamera(), "method_19325", yaw, pitch); // setRotation
		}catch(Exception e){
			info(Arrays.toString(e.getStackTrace()));
		}
        //mc.gameRenderer.getCamera().setPos(pos);
		//mc.gameRenderer.getCamera().setRotation(yaw, pitch);

    }

    
}