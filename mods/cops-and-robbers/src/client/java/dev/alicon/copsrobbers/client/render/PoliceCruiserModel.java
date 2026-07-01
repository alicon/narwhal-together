package dev.alicon.copsrobbers.client.render;

import dev.alicon.copsrobbers.CopsAndRobbers;
import dev.alicon.copsrobbers.entity.PoliceCruiserEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Low-poly model for the rideable Cops and Robbers cruiser. */
public final class PoliceCruiserModel extends EntityModel<PoliceCruiserRenderState> {
	/** Model layer used when baking the Cops and Robbers model. */
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CopsAndRobbers.id("police_cruiser"), "main");

	private final ModelPart frontLeftWheel;
	private final ModelPart frontRightWheel;
	private final ModelPart rearLeftWheel;
	private final ModelPart rearRightWheel;
	private final ModelPart lightbarRed;
	private final ModelPart lightbarBlue;
	private final ModelPart pushBar;
	private final ModelPart fireLadder;
	private final ModelPart waterCannon;

	/**
	 * Creates the model from a baked root part.
	 *
	 * @param root baked model root from the registered layer definition
	 */
	public PoliceCruiserModel(ModelPart root) {
		super(root);
		this.frontLeftWheel = root.getChild("front_left_wheel");
		this.frontRightWheel = root.getChild("front_right_wheel");
		this.rearLeftWheel = root.getChild("rear_left_wheel");
		this.rearRightWheel = root.getChild("rear_right_wheel");
		this.lightbarRed = root.getChild("lightbar_red");
		this.lightbarBlue = root.getChild("lightbar_blue");
		this.pushBar = root.getChild("push_bar");
		this.fireLadder = root.getChild("fire_ladder");
		this.waterCannon = root.getChild("water_cannon");
	}

	/** Builds the cuboid layer definition for the angular police truck. */
	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("lower_body", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-11.0F, -8.0F, -21.0F, 22.0F, 7.0F, 42.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("hood", CubeListBuilder.create()
				.texOffs(0, 50)
				.addBox(-9.5F, -11.0F, -24.0F, 19.0F, 5.0F, 17.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("cabin_left_wall", CubeListBuilder.create()
				.texOffs(76, 0)
				.addBox(7.0F, -17.0F, -8.0F, 1.0F, 9.0F, 20.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("cabin_right_wall", CubeListBuilder.create()
				.texOffs(76, 0)
				.addBox(-8.0F, -17.0F, -8.0F, 1.0F, 9.0F, 20.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("cabin_back_wall", CubeListBuilder.create()
				.texOffs(76, 0)
				.addBox(-8.0F, -17.0F, 11.0F, 16.0F, 9.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("dashboard", CubeListBuilder.create()
				.texOffs(76, 30)
				.addBox(-7.0F, -10.0F, -9.0F, 14.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("roof_peak", CubeListBuilder.create()
				.texOffs(72, 36)
				.addBox(-6.0F, -20.0F, -3.0F, 12.0F, 4.0F, 12.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("front_bumper", CubeListBuilder.create()
				.texOffs(0, 76)
				.addBox(-10.5F, -6.0F, -25.5F, 21.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("rear_bumper", CubeListBuilder.create()
				.texOffs(48, 76)
				.addBox(-10.5F, -6.0F, 22.5F, 21.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("windshield_top_frame", CubeListBuilder.create()
				.texOffs(120, 36)
				.addBox(-7.0F, -16.0F, -9.0F, 14.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("windshield_left_frame", CubeListBuilder.create()
				.texOffs(120, 42)
				.addBox(6.0F, -16.0F, -9.0F, 1.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("windshield_right_frame", CubeListBuilder.create()
				.texOffs(124, 42)
				.addBox(-7.0F, -16.0F, -9.0F, 1.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("windshield_bottom_frame", CubeListBuilder.create()
				.texOffs(128, 42)
				.addBox(-7.0F, -11.0F, -9.0F, 14.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("push_bar", CubeListBuilder.create()
				.texOffs(96, 72)
				.addBox(-8.5F, -10.0F, -27.0F, 17.0F, 6.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("side_mirror_left", CubeListBuilder.create()
				.texOffs(136, 52)
				.addBox(10.8F, -13.0F, -9.0F, 2.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("side_mirror_right", CubeListBuilder.create()
				.texOffs(144, 52)
				.addBox(-12.8F, -13.0F, -9.0F, 2.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("door_badge_left", CubeListBuilder.create()
				.texOffs(132, 0)
				.addBox(11.1F, -11.5F, -4.0F, 0.7F, 6.0F, 8.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("door_badge_right", CubeListBuilder.create()
				.texOffs(132, 14)
				.addBox(-11.8F, -11.5F, -4.0F, 0.7F, 6.0F, 8.0F), PartPose.offset(0.0F, 20.0F, 0.0F));

		root.addOrReplaceChild("lightbar_red", CubeListBuilder.create()
				.texOffs(0, 84)
				.addBox(-6.0F, -22.0F, 0.0F, 6.0F, 2.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("lightbar_blue", CubeListBuilder.create()
				.texOffs(24, 84)
				.addBox(0.0F, -22.0F, 0.0F, 6.0F, 2.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("fire_ladder", CubeListBuilder.create()
				.texOffs(48, 84)
				.addBox(-3.0F, -24.0F, -18.0F, 6.0F, 2.0F, 32.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("water_cannon", CubeListBuilder.create()
				.texOffs(124, 84)
				.addBox(-2.0F, -25.5F, -24.0F, 4.0F, 3.0F, 10.0F), PartPose.offset(0.0F, 20.0F, 0.0F));

		addWheel(root, "front_left_wheel", 10.5F, -13.5F);
		addWheel(root, "front_right_wheel", -10.5F, -13.5F);
		addWheel(root, "rear_left_wheel", 10.5F, 14.5F);
		addWheel(root, "rear_right_wheel", -10.5F, 14.5F);

		return LayerDefinition.create(mesh, 160, 96);
	}

	private static void addWheel(PartDefinition root, String name, float x, float z) {
		root.addOrReplaceChild(name, CubeListBuilder.create()
				.texOffs(104, 56)
				.addBox(-2.5F, -4.5F, -4.5F, 5.0F, 9.0F, 9.0F), PartPose.offset(x, 18.0F, z));
	}

	@Override
	public void setupAnim(PoliceCruiserRenderState state) {
		super.setupAnim(state);
		this.root.xRot = 0.0F;
		this.root.yRot = 0.0F;
		this.root.zRot = 0.0F;
		float wheelSpin = state.walkAnimationPos * 0.9F;
		this.frontLeftWheel.xRot = wheelSpin;
		this.frontRightWheel.xRot = wheelSpin;
		this.rearLeftWheel.xRot = wheelSpin;
		this.rearRightWheel.xRot = wheelSpin;

		this.pushBar.visible = !state.fireTruck;
		this.fireLadder.visible = state.fireTruck;
		this.waterCannon.visible = state.fireTruck;

		boolean flashRed = ((int) (state.ageInTicks / 6.0F)) % 2 == 0;
		this.lightbarRed.visible = state.lightsEnabled && flashRed;
		this.lightbarBlue.visible = state.lightsEnabled && !flashRed;

		float steer = Mth.sin(state.walkAnimationPos * 0.35F) * state.walkAnimationSpeed * 0.12F;
		this.frontLeftWheel.yRot = steer;
		this.frontRightWheel.yRot = steer;

		float spin = state.trickProgress * Mth.TWO_PI;
		if (state.trickType == PoliceCruiserEntity.TRICK_BARREL_ROLL) {
			this.root.zRot = spin;
		} else if (state.trickType == PoliceCruiserEntity.TRICK_LOOP) {
			this.root.xRot = -spin;
		}
	}
}
