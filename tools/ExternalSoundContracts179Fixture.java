public final class ExternalSoundContracts179Fixture {
    public static void main(String[] args)throws Exception {
        String base="com.mod.htgt6.minecraft1202.Module.Data.";
        String[] classes={"Modern.ModernCamelEntityFixture120","Modern.ModernGoatEntityFixture120",
                "Modern.ModernAllayEntityFixture120","Aquatic.AquaticCompletionContractFixture120"};
        for(String name:classes){
            java.lang.reflect.Method method=Class.forName(base+name).getDeclaredMethod(name.startsWith("Aquatic")?"verifySounds":"sounds");
            method.setAccessible(true);method.invoke(null);System.out.println("PASS external sound contract: "+name);
        }
    }
}
