package com.iviet.ivshs.util;

import java.util.Hashtable;

public class ConvertUnicode {
	private static Hashtable<String, String> hashUnicodeVi = new Hashtable<>();
	
//	public static void main(String[] args) {
//		String data = "011000E8n ph00F2ng t1EAFm 01";
//		
//		String result = convertUnicodeHexVi(data);
//		System.out.println(result);
//	}
	
	public static String convertUnicodeHexToVi(String data) {
		for (String key : hashUnicodeVi.keySet()) {
			data = data.replace(key, hashUnicodeVi.get(key));
		}
		return data;
	}
	
	static {
		hashUnicodeVi.put("00C0","À");
		hashUnicodeVi.put("00C1","Á");
		hashUnicodeVi.put("00C2","Â");
		hashUnicodeVi.put("00C3","Ã");
		hashUnicodeVi.put("00C8","È");
		hashUnicodeVi.put("00C9","É");
		hashUnicodeVi.put("00CA","Ê");
		hashUnicodeVi.put("00CC","Ì");
		hashUnicodeVi.put("00CD","Í");
		hashUnicodeVi.put("00D2","Ò");
		hashUnicodeVi.put("00D3","Ó");
		hashUnicodeVi.put("00D4","Ô");
		hashUnicodeVi.put("00D5","Õ");
		hashUnicodeVi.put("00D9","Ù");
		hashUnicodeVi.put("00DA","Ú");
		hashUnicodeVi.put("00DD","Ý");
		hashUnicodeVi.put("00E0","à");
		hashUnicodeVi.put("00E1","á");
		hashUnicodeVi.put("00E2","â");
		hashUnicodeVi.put("00E3","ã");
		hashUnicodeVi.put("00E8","è");
		hashUnicodeVi.put("00E9","é");
		hashUnicodeVi.put("00EA","ê");
		hashUnicodeVi.put("00EC","ì");
		hashUnicodeVi.put("00ED","í");
		hashUnicodeVi.put("00F2","ò");
		hashUnicodeVi.put("00F3","ó");
		hashUnicodeVi.put("00F4","ô");
		hashUnicodeVi.put("00F5","õ");
		hashUnicodeVi.put("00F9","ù");
		hashUnicodeVi.put("00FA","ú");
		hashUnicodeVi.put("00FD","ý");
		hashUnicodeVi.put("0102","Ă");
		hashUnicodeVi.put("0103","ă");
		hashUnicodeVi.put("0110","Đ");
		hashUnicodeVi.put("0111","đ");
		hashUnicodeVi.put("0128","Ĩ");
		hashUnicodeVi.put("0129","ĩ");
		hashUnicodeVi.put("0168","Ũ");
		hashUnicodeVi.put("0169","ũ");
		hashUnicodeVi.put("01A0","Ơ");
		hashUnicodeVi.put("01A1","ơ");
		hashUnicodeVi.put("01AF","Ư");
		hashUnicodeVi.put("01B0","ư");
		hashUnicodeVi.put("1EA0","Ạ");
		hashUnicodeVi.put("1EA1","ạ");
		hashUnicodeVi.put("1EA2","Ả");
		hashUnicodeVi.put("1EA3","ả");
		hashUnicodeVi.put("1EA4","Ấ");
		hashUnicodeVi.put("1EA5","ấ");
		hashUnicodeVi.put("1EA6","Ầ");
		hashUnicodeVi.put("1EA7","ầ");
		hashUnicodeVi.put("1EA8","Ẩ");
		hashUnicodeVi.put("1EA9","ẩ");
		hashUnicodeVi.put("1EAA","Ẫ");
		hashUnicodeVi.put("1EAB","ẫ");
		hashUnicodeVi.put("1EAC","Ậ");
		hashUnicodeVi.put("1EAD","ậ");
		hashUnicodeVi.put("1EAE","Ắ");
		hashUnicodeVi.put("1EAF","ắ");
		hashUnicodeVi.put("1EB0","Ằ");
		hashUnicodeVi.put("1EB1","ằ");
		hashUnicodeVi.put("1EB2","Ẳ");
		hashUnicodeVi.put("1EB3","ẳ");
		hashUnicodeVi.put("1EB4","Ẵ");
		hashUnicodeVi.put("1EB5","ẵ");
		hashUnicodeVi.put("1EB6","Ặ");
		hashUnicodeVi.put("1EB7","ặ");
		hashUnicodeVi.put("1EB8","Ẹ");
		hashUnicodeVi.put("1EB9","ẹ");
		hashUnicodeVi.put("1EBA","Ẻ");
		hashUnicodeVi.put("1EBB","ẻ");
		hashUnicodeVi.put("1EBC","Ẽ");
		hashUnicodeVi.put("1EBD","ẽ");
		hashUnicodeVi.put("1EBE","Ế");
		hashUnicodeVi.put("1EBF","ế");
		hashUnicodeVi.put("1EC0","Ề");
		hashUnicodeVi.put("1EC1","ề");
		hashUnicodeVi.put("1EC2","Ể");
		hashUnicodeVi.put("1EC3","ể");
		hashUnicodeVi.put("1EC4","Ễ");
		hashUnicodeVi.put("1EC5","ễ");
		hashUnicodeVi.put("1EC6","Ệ");
		hashUnicodeVi.put("1EC7","ệ");
		hashUnicodeVi.put("1EC8","Ỉ");
		hashUnicodeVi.put("1EC9","ỉ");
		hashUnicodeVi.put("1ECA","Ị");
		hashUnicodeVi.put("1ECB","ị");
		hashUnicodeVi.put("1ECC","Ọ");
		hashUnicodeVi.put("1ECD","ọ");
		hashUnicodeVi.put("1ECE","Ỏ");
		hashUnicodeVi.put("1ECF","ỏ");
		hashUnicodeVi.put("1ED0","Ố");
		hashUnicodeVi.put("1ED1","ố");
		hashUnicodeVi.put("1ED2","Ồ");
		hashUnicodeVi.put("1ED3","ồ");
		hashUnicodeVi.put("1ED4","Ổ");
		hashUnicodeVi.put("1ED5","ổ");
		hashUnicodeVi.put("1ED6","Ỗ");
		hashUnicodeVi.put("1ED7","ỗ");
		hashUnicodeVi.put("1ED8","Ộ");
		hashUnicodeVi.put("1ED9","ộ");
		hashUnicodeVi.put("1EDA","Ớ");
		hashUnicodeVi.put("1EDB","ớ");
		hashUnicodeVi.put("1EDC","Ờ");
		hashUnicodeVi.put("1EDD","ờ");
		hashUnicodeVi.put("1EDE","Ở");
		hashUnicodeVi.put("1EDF","ở");
		hashUnicodeVi.put("1EE0","Ỡ");
		hashUnicodeVi.put("1EE1","ỡ");
		hashUnicodeVi.put("1EE2","Ợ");
		hashUnicodeVi.put("1EE3","ợ");
		hashUnicodeVi.put("1EE4","Ụ");
		hashUnicodeVi.put("1EE5","ụ");
		hashUnicodeVi.put("1EE6","Ủ");
		hashUnicodeVi.put("1EE7","ủ");
		hashUnicodeVi.put("1EE8","Ứ");
		hashUnicodeVi.put("1EE9","ứ");
		hashUnicodeVi.put("1EEA","Ừ");
		hashUnicodeVi.put("1EEB","ừ");
		hashUnicodeVi.put("1EEC","Ử");
		hashUnicodeVi.put("1EED","ử");
		hashUnicodeVi.put("1EEE","Ữ");
		hashUnicodeVi.put("1EEF","ữ");
		hashUnicodeVi.put("1EF0","Ự");
		hashUnicodeVi.put("1EF1","ự");
		hashUnicodeVi.put("1EF2","Ỳ");
		hashUnicodeVi.put("1EF3","ỳ");
		hashUnicodeVi.put("1EF4","Ỵ");
		hashUnicodeVi.put("1EF5","ỵ");
		hashUnicodeVi.put("1EF6","Ỷ");
		hashUnicodeVi.put("1EF7","ỷ");
		hashUnicodeVi.put("1EF8","Ỹ");
		hashUnicodeVi.put("1EF9","ỹ");
	}
}
