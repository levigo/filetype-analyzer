/**
 * Copyright (c) 1995-2015 levigo holding gmbh. All Rights Reserved.
 *
 * This software is the proprietary information of levigo holding gmbh.
 * Use is subject to license terms.
 */
package org.jadice.filetype.matchers.modca;

/**
 * A utility class used to hold detailed information about MO:DCA structs.
 */
final class ModcaStructInfo implements MODCAConstants {

	static ModcaStructInfo structInfos[] = {
			new ModcaStructInfo(IPG, "IPG", "Include Page"),
			new ModcaStructInfo(MPG, "MPG", "Map Page"),
			new ModcaStructInfo(MFC, "MFC", "Medium Finishing Control"),
			new ModcaStructInfo(PFC, "PFC", "Presentaion Fidelity Control"),
			new ModcaStructInfo(TLE, "TLE", "Tag Logical Element"),
			new ModcaStructInfo(MCC, "MCC", "Medium Copy Count"),
			new ModcaStructInfo(OBD, "OBD", "Object Area Descriptor"),
			new ModcaStructInfo(IID, "IID", "IM Image Input Descriptor (C)"),
			new ModcaStructInfo(MDD, "MDD", "Medium Descriptor"),
			new ModcaStructInfo(CDD, "CDD", "Container Data Descriptor"),
			new ModcaStructInfo(PTD1, "PTD1",
					"Presentation Text Descriptor Format-1 (C)"),
			new ModcaStructInfo(PGD, "PGD", "Page Descriptor"),
			new ModcaStructInfo(GDD, "GDD", "Graphics Data Descriptor"),
			new ModcaStructInfo(FGD, "FGD", "Form Environment Group Descriptor (O)"),
			new ModcaStructInfo(BDD, "BDD", "Bar Code Data Descriptor"),
			new ModcaStructInfo(IDD, "IDD", "Image Data Descriptor"),
			new ModcaStructInfo(IOC, "IOC", "IM Image Output Control (C)"),
			new ModcaStructInfo(MMC, "MMC", "Medium Modification Control"),
			new ModcaStructInfo(CTC, "CTC", "Composed Text Control (O)"),
			new ModcaStructInfo(PMC, "PMC", "Page Modification Control"),
			new ModcaStructInfo(BPS, "BPS", "Begin Page Segment"),
			new ModcaStructInfo(BCA, "BCA", "Begin Color Attribute Table"),
			new ModcaStructInfo(BII, "BII", "Begin IM Image (C)"),
			new ModcaStructInfo(BOC, "BOC", "Begin Object Container"),
			new ModcaStructInfo(BPT, "BPT", "Begin Presentation Text Object"),
			new ModcaStructInfo(BDI, "BDI", "Begin Document Index"),
			new ModcaStructInfo(BDT, "BDT", "Begin Document"),
			new ModcaStructInfo(BNG, "BNG", "Begin Named Page Group"),
			new ModcaStructInfo(BPG, "BPG", "Begin Page"),
			new ModcaStructInfo(BGR, "BGR", "Begin Graphics Object"),
			new ModcaStructInfo(BDG, "BDG", "Begin Document Environment Group"),
			new ModcaStructInfo(BFG, "BFG", "Begin Form Environment Group (O)"),
			new ModcaStructInfo(BRG, "BRG", "Begin Resource Group"),
			new ModcaStructInfo(BOG, "BOG", "Begin Object Environment Group"),
			new ModcaStructInfo(BAG, "BAG", "Begin Active Environment Group"),
			new ModcaStructInfo(BMM, "BMM", "Begin Medium Map"),
			new ModcaStructInfo(BFM, "BFM", "Begin Form Map"),
			new ModcaStructInfo(BR, "BR", "Begin Resource (R)"),
			new ModcaStructInfo(BMO, "BMO", "Begin Overlay"),
			new ModcaStructInfo(BBC, "BBC", "Begin Bar Code Object"),
			new ModcaStructInfo(BIM, "BIM", "Begin Image Object"),
			new ModcaStructInfo(EPS, "EPS", "End Page Segment"),
			new ModcaStructInfo(ECA, "ECA", "End Color Attribute Table"),
			new ModcaStructInfo(EII, "EII", "End IM Image (C)"),
			new ModcaStructInfo(EOC, "EOC", "End Object Container"),
			new ModcaStructInfo(EPT, "EPT", "End Presentation Text Object"),
			new ModcaStructInfo(EDI, "EDI", "End Document Index"),
			new ModcaStructInfo(EDT, "EDT", "End Document"),
			new ModcaStructInfo(ENG, "ENG", "End Named Page Group"),
			new ModcaStructInfo(EPG, "EPG", "End Page"),
			new ModcaStructInfo(EGR, "EGR", "End Graphics Object"),
			new ModcaStructInfo(EDG, "EDG", "End Document Environment Group"),
			new ModcaStructInfo(EFG, "EFG", "End Form Environment Group (O)"),
			new ModcaStructInfo(ERG, "ERG", "End Resource Group"),
			new ModcaStructInfo(EOG, "EOG", "End Object Environment Group"),
			new ModcaStructInfo(EAG, "EAG", "End Active Environment Group"),
			new ModcaStructInfo(EMM, "EMM", "End Medium Map"),
			new ModcaStructInfo(EFM, "EFM", "End Form Map"),
			new ModcaStructInfo(ER, "ER", "End Resource (R)"),
			new ModcaStructInfo(EMO, "EMO", "End Overlay"),
			new ModcaStructInfo(EBC, "EBC", "End Bar Code Object"),
			new ModcaStructInfo(EIM, "EIM", "End Image Object"),
			new ModcaStructInfo(MCA, "MCA", "Map Color Attribute Table"),
			new ModcaStructInfo(MCF, "MCF", "Map Coded Font"),
			new ModcaStructInfo(MCD, "MCD", "Map Container Data"),
			new ModcaStructInfo(MGO, "MGO", "Map Graphics Object"),
			new ModcaStructInfo(MDR, "MDR", "Map Data Resource"),
			new ModcaStructInfo(IMM, "IMM", "Invoke Medium Map"),
			new ModcaStructInfo(MPO, "MPO", "Map Page Overlay"),
			new ModcaStructInfo(MSU, "MSU", "Map Suppression"),
			new ModcaStructInfo(MBC, "MBC", "Map Bar Code Object"),
			new ModcaStructInfo(MIO, "MIO", "Map Image Object"),
			new ModcaStructInfo(OBP, "OBP", "Object Area Position"),
			new ModcaStructInfo(ICP, "ICP", "IM Image Cell Position (C)"),
			new ModcaStructInfo(PGP1, "PGP1", "Page Position Format-1 (C)"),
			new ModcaStructInfo(IPS, "IPS", "Include Page Segment"),
			new ModcaStructInfo(IOB, "IOB", "Include Object"),
			new ModcaStructInfo(IPO, "IPO", "Include Page Overlay"),
			new ModcaStructInfo(CAT, "CAT", "Color Attribute Table"),
			new ModcaStructInfo(MPS, "MPS", "Map Page Segment"),
			new ModcaStructInfo(MCF1, "MCF1", "Map Coded Font Format-1 (C)"),
			new ModcaStructInfo(PTD, "PTD", "Presentation Text Data Descriptor"),
			new ModcaStructInfo(PGP, "PGP", "Page Position"),
			new ModcaStructInfo(MMO, "MMO", "Map Medium Overlay"),
			new ModcaStructInfo(IEL, "IEL", "Index Element"),
			new ModcaStructInfo(LLE, "LLE", "Link Logical Element"),
			new ModcaStructInfo(IRD, "IRD", "IM Image Raster Data (C)"),
			new ModcaStructInfo(OCD, "OCD", "Object Container Data"),
			new ModcaStructInfo(PTX, "PTX", "Presentation Text Data"),
			new ModcaStructInfo(GAD, "GAD", "Graphics Data"),
			new ModcaStructInfo(BDA, "BDA", "Bar Code Data"),
			new ModcaStructInfo(NOP, "NOP", "No Operation"),
			new ModcaStructInfo(IPD, "IPD", "Image Picture Data"),
			new ModcaStructInfo(BCF, "BCF", "Begin Coded Font"),
			new ModcaStructInfo(BCP, "BCP", "Begin Code Page"),
			new ModcaStructInfo(BFN, "BFN", "Begin Font"),
			new ModcaStructInfo(CFC, "CFC", "Coded Font Control"),
			new ModcaStructInfo(CFI, "CFI", "Coded Font Index"),
			new ModcaStructInfo(CPC, "CPC", "Code Page Control"),
			new ModcaStructInfo(CPD, "CPD", "Code Page Descriptor"),
			new ModcaStructInfo(CPI, "CPI", "Code Page Index"),
			new ModcaStructInfo(ECF, "ECF", "End Coded Font"),
			new ModcaStructInfo(ECP, "ECP", "End Code Page"),
			new ModcaStructInfo(EFN, "EFN", "End Font"),
			new ModcaStructInfo(FNC, "FNC", "Font Control"),
			new ModcaStructInfo(FND, "FND", "Font Descriptor"),
			new ModcaStructInfo(FNG, "FNG", "Font Patterns"),
			new ModcaStructInfo(FNI, "FNI", "Font Index"),
			new ModcaStructInfo(FNM, "FNM", "Font Patterns Map"),
			new ModcaStructInfo(FNN, "FNN", "Font Name Map"),
			new ModcaStructInfo(FNO, "FNO", "Font Orientation"),
			new ModcaStructInfo(FNP, "FNP", "Font Position"),};

	private String description;
	private String shortName;
	private int structID;

	private ModcaStructInfo() {
		// prevent instantiation
	}

	private ModcaStructInfo(final int id, final String name, final String description) {
		this.structID = id;
		this.shortName = name;
		this.description = description;
	}

	public static ModcaStructInfo getNameByID(final int id) {
		for (int i = 0; i < structInfos.length; i++) {
			if (structInfos[i].structID == id)
				return structInfos[i];
		}
		return new ModcaStructInfo(id, "<UNKNOWN (0x" + Integer.toHexString(id)
				+ ")>", "Unknown struct");
	}

	public String getDescription() {
		return description;
	}

	public String getShortName() {
		return shortName;
	}

	public int getStructID() {
		return structID;
	}

}
