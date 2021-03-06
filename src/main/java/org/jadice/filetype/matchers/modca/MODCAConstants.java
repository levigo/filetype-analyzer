package org.jadice.filetype.matchers.modca;

interface MODCAConstants {
	static final int ROTATION_000 = 0x0000;
	static final int ROTATION_090 = 0x2D00;
	static final int ROTATION_180 = 0x5A00;
	static final int ROTATION_270 = 0x8700;
	
  static final int BAG = 0xA8C9; // Begin Active Environment Group
  static final int BBC = 0xA8EB; // Begin Bar Code Object
  static final int BCA = 0xA877; // Begin Color Attribute Table
  static final int BCF = 0xA88A; // Begin Coded Font
  static final int BCP = 0xA887; // Begin Code Page
  static final int BDA = 0xEEEB; // Bar Code Data
  // Form Environment Group Descriptor (O)
  static final int BDD = 0xA6EB; // Bar Code Data Descriptor
  static final int BDG = 0xA8C4; // Begin Document Environment Group
  static final int BDI = 0xA8A7; // Begin Document Index
  static final int BDT = 0xA8A8; // Begin Document
  static final int BFG = 0xA8C5; // Begin Form Environment Group (O)
  static final int BFM = 0xA8CD; // Begin Form Map
  static final int BFN = 0xA889; // Begin Font
  static final int BGR = 0xA8BB; // Begin Graphics Object
  static final int BII = 0xA87B; // Begin IM Image (C)
  static final int BIM = 0xA8FB; // Begin Image Object
  static final int BMM = 0xA8CC; // Begin Medium Map
  static final int BMO = 0xA8DF; // Begin Overlay
  static final int BNG = 0xA8AD; // Begin Named PageSegment Group
  static final int BOC = 0xA892; // Begin Object Container
  static final int BOG = 0xA8C7; // Begin Object Environment Group
  static final int BPG = 0xA8AF; // Begin PageSegment
  static final int BPS = 0xA85F; // Begin PageSegment Segment
  static final int BPT = 0xA89B; // Begin Presentation Text Object
  static final int BR = 0xA8CE; // Begin Resource (R)
  static final int BRG = 0xA8C6; // Begin Resource Group
  static final int CAT = 0xB077; // Color Attribute Table
  static final int CDD = 0xA692; // Container Data Descriptor
  static final int CFC = 0xA78A; // Coded Font Control
  static final int CTC = 0xA79B; // Composed Text Control (O)
  static final int EAG = 0xA9C9; // End Active Environment Group
  static final int EBC = 0xA9EB; // End Bar Code Object
  static final int ECA = 0xA977; // End Color Attribute Table
  static final int EDG = 0xA9C4; // End Document Environment Group
  static final int EDI = 0xA9A7; // End Document Index
  static final int EDT = 0xA9A8; // End Document
  static final int EFG = 0xA9C5; // End Form Environment Group (O)
  static final int EFM = 0xA9CD; // End Form Map
  static final int EGR = 0xA9BB; // End Graphics Object
  static final int EII = 0xA97B; // End IM Image (C)
  static final int EIM = 0xA9FB; // End Image Object
  static final int EMM = 0xA9CC; // End Medium Map
  static final int EMO = 0xA9DF; // End Overlay
  static final int ENG = 0xA9AD; // End Named PageSegment Group
  static final int EOC = 0xA992; // End Object Container
  static final int EOG = 0xA9C7; // End Object Environment Group
  static final int EPG = 0xA9AF; // End PageSegment
  static final int EPS = 0xA95F; // End PageSegment Segment
  static final int EPT = 0xA99B; // End Presentation Text Object
  static final int ER = 0xA9CE; // End Resource (R)
  static final int ERG = 0xA9C6; // End Resource Group
  static final int FGD = 0xA6C5;
  static final int FNG = 0xEE89; // Font Patterns
  static final int GAD = 0xEEBB; // Graphics Data
  static final int GDD = 0xA6BB; // Graphics Data Descriptor
  static final int ICP = 0xAC7B; // IM Image Cell Position (C)
  static final int IDD = 0xA6FB; // Image Data Descriptor
  static final int IEL = 0xB2A7; // Index Element
  static final int IID = 0xA67B; // IM Image Input Descriptor (C)
  static final int IMM = 0xABCC; // Invoke Medium Map
  static final int IOB = 0xAFC3; // Include Object
  static final int IOC = 0xA77B; // IM Image Output Control (C)
  static final int IPD = 0xEEFB; // Image Picture Data
  // IBM Defined Objects
  static final int IPG = 0xAFAF; // Include PageSegment
  static final int IPO = 0xAFD8; // Include PageSegment Overlay
  static final int IPS = 0xAF5F; // Include PageSegment Segment
  static final int IRD = 0xEE7B; // IM Image Raster Data (C)
  static final int LLE = 0xB490; // Link Logical Element
  static final int MBC = 0xABEB; // Map Bar Code Object
  static final int MCA = 0xAB77; // Map Color Attribute Table
  static final int MCC = 0xA288; // Medium Copy Count
  static final int MCD = 0xAB92; // Map Container Data
  static final int MCF = 0xAB8A; // Map Coded Font
  static final int MCF1 = 0xB18A; // Map Coded Font Format-1 (C)
  static final int MDD = 0xA688; // Medium Descriptor
  static final int MDR = 0xABC3; // Map Data Resource
  static final int MFC = 0xA088; // Medium Finishing Control
  static final int MGO = 0xABBB; // Map Graphics Object
  static final int MIO = 0xABFB; // Map Image Object
  static final int MMC = 0xA788; // Medium Modification Control
  static final int MMO = 0xB1DF; // Map Medium Overlay
  static final int MPG = 0xABAF; // Map PageSegment
  static final int MPO = 0xABD8; // Map PageSegment Overlay
  static final int MPS = 0xB15F; // Map PageSegment Segment
  static final int MSU = 0xABEA; // Map Suppression
  static final int NOP = 0xEEEE; // No Operation
  static final int OBD = 0xA66B; // Object Area Descriptor
  static final int OBP = 0xAC6B; // Object Area Position
  static final int OCD = 0xEE92; // Object Container Data
  static final int PFC = 0xB288; // Presentaion Fidelity Control
  // Presentation Text Descriptor Format-1 (C)
  static final int PGD = 0xA6AF; // PageSegment Descriptor
  static final int PGP = 0xB1AF; // PageSegment Position
  static final int PGP1 = 0xACAF; // PageSegment Position Format-1 (C)
  static final int PMC = 0xA7AF; // PageSegment Modification Control
  static final int PTD = 0xB19B; // Presentation Text Data Descriptor
  static final int PTD1 = 0xA69B;
  static final int PTX = 0xEE9B; // Presentation Text Data
  static final int TLE = 0xA090; // Tag Logical Element
  static final int CFI = 0x8C8A; // Coded Font Index
  static final int CPC = 0xA787; // Code Page Control
  static final int CPD = 0xA687; // Code Page Descriptor
  static final int CPI = 0x8C87; // Code Page Index
  static final int ECF = 0xA98A; // End Coded Font
  static final int ECP = 0xA987; // End Code Page
  static final int EFN = 0xA989; // End Font
  static final int FNC = 0xA789; // Font Control
  static final int FND = 0xA689; // Font Descriptor
  static final int FNI = 0x8C89; // Font Index  
  static final int FNM = 0xA289; // Font Patterns Map
  static final int FNN = 0xAB89; // Font Name Map
  static final int FNO = 0xAE89; // Font Orientation
  static final int FNP = 0xAC89; // Font Position

}
