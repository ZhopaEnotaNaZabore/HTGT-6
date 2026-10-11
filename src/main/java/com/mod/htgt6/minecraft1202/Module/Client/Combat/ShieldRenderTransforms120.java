package com.mod.htgt6.minecraft1202.Module.Client.Combat;

/** Vanilla 1.20.2 shield.json and shield_blocking.json hand transforms. */
public final class ShieldRenderTransforms120 {
    /** Shield uses modern model units at the camera-space hook, without legacy item shrinking. */
    public static final float FIRST_PERSON_17_ADAPTER_SCALE = 1.0F;
    public static final Transform FIRST_PERSON_LEFT =
            new Transform(10.0F, 0.0F, -10.0F, 0.0F, 180.0F, 5.0F, 1.25F);
    public static final Transform FIRST_PERSON_LEFT_BLOCKING =
            new Transform(5.0F, 5.0F, -11.0F, 0.0F, 180.0F, -5.0F, 1.25F);
    public static final Transform FIRST_PERSON_RIGHT =
            new Transform(-10.0F, 2.0F, -10.0F, 0.0F, 180.0F, 5.0F, 1.25F);
    public static final Transform FIRST_PERSON_RIGHT_BLOCKING =
            new Transform(-15.0F, 5.0F, -11.0F, 0.0F, 180.0F, -5.0F, 1.25F);
    public static final Transform THIRD_PERSON_LEFT =
            new Transform(10.0F, 6.0F, 12.0F, 0.0F, 90.0F, 0.0F, 1.0F);
    public static final Transform THIRD_PERSON_LEFT_BLOCKING =
            new Transform(13.51F, 3.0F, 5.0F, 45.0F, 135.0F, 0.0F, 1.0F);
    public static final Transform THIRD_PERSON_RIGHT =
            new Transform(10.0F, 6.0F, -4.0F, 0.0F, 90.0F, 0.0F, 1.0F);
    public static final Transform THIRD_PERSON_RIGHT_BLOCKING =
            new Transform(3.51F, 11.0F, -2.0F, 45.0F, 135.0F, 0.0F, 1.0F);

    /** Mojang ItemTransform.apply: only X translation and Y/Z angles change sign for left hand. */
    public static float[] components(Transform transform, boolean left) {
        float sign = left ? -1.0F : 1.0F;
        return new float[]{sign * transform.x / 16.0F, transform.y / 16.0F, transform.z / 16.0F,
                transform.rotationX, sign * transform.rotationY, sign * transform.rotationZ, transform.scale};
    }

    public static final class Transform {
        /** Translation is stored in the model JSON's one-sixteenth-block units. */
        public final float x, y, z;
        public final float rotationX, rotationY, rotationZ;
        public final float scale;

        private Transform(float x, float y, float z, float rotationX, float rotationY,
                float rotationZ, float scale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
            this.scale = scale;
        }
    }

    private ShieldRenderTransforms120() { }
}
