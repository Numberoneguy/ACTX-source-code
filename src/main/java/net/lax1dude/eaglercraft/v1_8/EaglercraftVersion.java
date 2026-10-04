package net.lax1dude.eaglercraft.v1_8;

import java.math.BigInteger;

public class EaglercraftVersion {
	
	
	//////////////////////////////////////////////////////////////////////
	
	/// Customize these to fit your fork:
	
	public static final String projectForkName = "ActX";
	public static final String projectForkVersion = "v1.7.0";
	public static final String projectForkVendor = "Numberoneguy";
	
	public static final String projectForkURL = "https://gitlab.com/Numberoneguy/yesRepo/tree/main/Release";
	
	//////////////////////////////////////////////////////////////////////
	
	public static final String projectOriginName = "EaglercraftX";
	public static final String projectOriginAuthor = "lax1dude";
	public static final String projectOriginRevision = "1.8";
	public static final String projectOriginVersion = "u53";
	
	public static final String projectOriginURL = "https://gitlab.com/lax1dude/eaglercraftx-1.8"; // rest in peace
	
	// EPK Version Identifier
	
	public static final String EPKVersionIdentifier = "null"; // Set to null to disable EPK version check
	
	// Updating configuration
	
	public static final boolean enableUpdateService = false;

	public static final String updateBundlePackageName = "net.lax1dude.eaglercraft.v1_8.client";
	public static final int updateBundlePackageVersionInt = 53;

	public static final String updateLatestLocalStorageKey = "latestUpdate_" + updateBundlePackageName;

	// public key modulus for official 1.8 updates
	public static final BigInteger updateSignatureModulus = new BigInteger("21804451660775392763550109945008633319774486401558098727845027043465910019423884059856907310257373763849388442299025832834365102723474704625715722541486456370793700918656109847619403851428974545763931666566414001194848198905510503349453920173739636161963528116026257017663869183165816067507577009091068971345028212001753705148475544883265707434998417436589203875208669094517450628261519444723198824773985839840507167035753438218197153788675314935303129715369447388025657181217180942052345109287380242859617525168603455448872977221320454662323854625081982103901973374705101204624940302442635937235452184439991455517243");
	
	
	
	// Client brand identification system configuration
	
	public static final EaglercraftUUID clientBrandUUID = EagUtils.makeClientBrandUUID(projectForkName);

	public static final EaglercraftUUID legacyClientUUIDInSharedWorld = EagUtils.makeClientBrandUUIDLegacy(projectOriginName);
	
	
	// Miscellaneous variables:

	public static final String mainMenuStringA = "Minecraft 1.8.8";
	public static final String mainMenuStringB = projectOriginName + " " + projectOriginRevision + "-"
			+ projectOriginVersion + " ultimate [" + EagRuntime.getPlatformType().getName() + "]";
	public static final String mainMenuStringC = "";
	public static final String mainMenuStringD = "Resources Copyright Mojang AB";

	public static final String mainMenuStringE = projectForkName + " " + projectForkVersion;
	public static final String mainMenuStringF = "Made by " + projectForkVendor;

	public static final String mainMenuStringG = "Collector's Edition";
	public static final String mainMenuStringH = "PBR Shaders";

	public static final String screenRecordingFilePrefix = projectOriginName + " "
			+ projectOriginRevision + "-" + projectOriginVersion;

	public static final long demoWorldSeed = (long) "North Carolina".hashCode();

	public static final boolean mainMenuEnableGithubButton = false;

	public static final boolean forceDemoMode = false;

	public static final String localStorageNamespace = "_eaglercraftX";

}
