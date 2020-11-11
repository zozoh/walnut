package org.nutz.walnut.ext.abb.hdl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class abb_ckimp extends abb_abstract_ckimp {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		String tmp = hc.params.val_check(0);
		String aph = Wn.normalizeFullPath(tmp, sys);
        WnObj wobj = sys.io.check(null, aph);
		if (!wobj.isFILE()) {
			sys.err.print("must be file");
			return;
		}
		InputStream ins = sys.io.getInputStream(wobj, 0);
		XSSFWorkbook wb = new XSSFWorkbook(ins);
		ins.close();
		String mode = hc.params.get("mode", "A");
		switch (mode) {
		case "A":
			mode_a(sys, hc, wb);
			break;
		case "B":
			mode_b(sys, hc, wb);
			break;

		default:
			break;
		}
		wb.close();

		wb.close();
	}

	protected void mode_a(WnSystem sys, JvmHdlContext hc, XSSFWorkbook wb) {
		XSSFSheet preSheet = wb.getSheet("PRE+BCE+CCE");
		XSSFSheet descSheet = wb.getSheet("Description");
		if (preSheet == null) {
			sys.err.print("miss Sheet : PRE+BCE+CCE");
			return;
		}
		if (descSheet == null) {
			sys.err.print("miss Sheet : Description");
			return;
		}

		int rowIndex = 4;
		String reportValue = null, groupValue = null, itemValue = null;
		Map<String, AbbCheckListItem> items = new LinkedHashMap<>();
		List<AbbCheckListItem> list = new ArrayList<>();
		while (true) {
			XSSFRow row = preSheet.getRow(rowIndex);
			rowIndex++;
			if (row == null)
				break;
			XSSFCell reportCell = row.getCell(0);
			XSSFCell groupCell = row.getCell(1);
			XSSFCell noCell = row.getCell(2);
			XSSFCell itemCell = row.getCell(3);

			if (groupCell == null)
				continue;
			String _reportValue = reportCell.getStringCellValue();
			String _groupValue = groupCell.getStringCellValue();
			Object tmp = __get_cell_value(noCell);
			String _noValue = null;
			if (tmp != null) {
				if (tmp instanceof Number) {
					_noValue = String.format("%.1f", tmp);
				} else {
					_noValue = tmp.toString().trim();
				}
			}
			String _itemValue = itemCell.getStringCellValue();

			if (_noValue == null)
				continue;

			if (Strings.isBlank(_reportValue))
				_reportValue = reportValue;
			else
				reportValue = _reportValue;
			if (Strings.isBlank(_groupValue))
				_groupValue = groupValue;
			else
				groupValue = _groupValue;
			if (Strings.isBlank(_itemValue))
				_itemValue = itemValue;
			else
				itemValue = _itemValue;

			//

//			System.out.printf("%s, %s, %s, %s\r\n", "", 
//					_groupValue,
//					_noValue,
//					_itemValue);
			items.put(_noValue, new AbbCheckListItem(_noValue, _groupValue, _itemValue));
		}

		rowIndex = 6;
		while (true) {
			XSSFRow row = descSheet.getRow(rowIndex);
			rowIndex++;
			if (row == null)
				break;
			XSSFCell noCell = row.getCell(3);
			String _noValue = __get_cell_value(noCell) == null ? null : __get_cell_value(noCell).toString();
			if (_noValue == null)
				continue;
			AbbCheckListItem item = items.get(_noValue);
			if (item == null) {
				log.info("miss 序号对应的描述信息" + _noValue);
				continue;
			}
			item.desc = row.getCell(5).getStringCellValue();
			item.rs = row.getCell(6).getStringCellValue();
			item.sf = row.getCell(7).getStringCellValue();
			item.outfiles = (String) __get_cell_value(row.getCell(8));
			item.owner = (String) __get_cell_value(row.getCell(9));

			list.add(item);
		}

//		sys.out.writeJson(list, JsonFormat.full());
		
		AbbCheckList clist = new AbbCheckList();
		Set<String> parents = new HashSet<>();
		for (AbbCheckListItem abbCheckListItem : list) {
			if (!parents.contains(abbCheckListItem.parentStr)) {
				AbbCheckListAItem aitem = new AbbCheckListAItem();
				aitem.nm = abbCheckListItem.parentStr.split(" ", 2)[0];
				aitem.title = abbCheckListItem.parentStr.split(" ", 2)[1];
				clist.items.add(aitem);
				parents.add(abbCheckListItem.parentStr);
			}
			AbbCheckListAItem aitem = new AbbCheckListAItem();
			aitem.nm = abbCheckListItem.no;
			aitem.title = abbCheckListItem.selfStr;
			aitem.p_ck_nm = abbCheckListItem.parentStr.split(" ", 2)[0];
			clist.items.add(aitem);
		}
		sys.out.writeJson(clist, JsonFormat.full());
	}
	

	// 首先, 查找起始行
	int rowBegin = -1;
	int workHourCellIndex = -1;
	int groupCellIndex = 0;
	
	protected XSSFSheet mode_b_search_group(WnSystem sys, JvmHdlContext hc, XSSFWorkbook wb) {
		rowBegin = -1;
		workHourCellIndex = -1;
		
		for (int z = 0; z < wb.getNumberOfSheets(); z++) {
			XSSFSheet preSheet = wb.getSheetAt(z);
			OUT: for (int i = 0; i < 16; i++) {
				XSSFRow row = preSheet.getRow(i);
				if (row == null)
					continue;
				for (int k = 0; k < 16; k++) {
					XSSFCell cell = row.getCell(k);
					if (cell == null)
						continue;
					String value = String.valueOf(__get_cell_value(cell)).trim().toLowerCase();
					if ("group".equals(value)) {
						rowBegin = i;
						groupCellIndex = k;
						// 找一下workhour的列
						for (int j = 1; j < 26; j++) {
							cell = row.getCell(j);
							if (cell == null)
								continue;
							value = String.valueOf(__get_cell_value(cell)).trim().toLowerCase();
							if ("workhour".equals(value)) {
								workHourCellIndex = j;
							}
							else if ("wh".equals(value)){
								workHourCellIndex = j;
							}
						}
						break OUT;
					}
				}
			}
			if (rowBegin >= 0) {
				return preSheet;
			}
		}
		return null;
	}


	protected void mode_b(WnSystem sys, JvmHdlContext hc, XSSFWorkbook wb) {
		XSSFSheet preSheet = mode_b_search_group(sys, hc, wb);
		if (rowBegin < 0) {
			// 找不到, 报错退出
			sys.err.print("miss row for Group!!!");
			return;
		}

		int rowIndex = rowBegin + 1;
		String preGourp = null;
		Map<String, AbbCheckListItem> items = new LinkedHashMap<>();
		List<AbbCheckListItem> list = new ArrayList<>();
		while (true) {
			XSSFRow row = preSheet.getRow(rowIndex);
			rowIndex++;
			if (row == null)
				break;
			XSSFCell groupCell = row.getCell(groupCellIndex+0);
			XSSFCell noCell = row.getCell(groupCellIndex+1);
			XSSFCell itemCell = row.getCell(groupCellIndex+2);

			if (groupCell == null)
				continue;
			String groupValue = groupCell.getStringCellValue();
			Object tmp = __get_cell_value(noCell);
			if (tmp == null)
				continue;
			String noValue = null;
			if (tmp instanceof Number) {
				noValue = String.format("%.1f", tmp);
			} else {
				noValue = tmp.toString().trim();
			}
			String itemValue = itemCell.getStringCellValue();

			if (noValue == null)
				continue;
			// 如果Group是空, 那就是继承上一个
			if (Strings.isBlank(groupValue))
				groupValue = preGourp;
			// 如果不是, 那就是新的Group了
			else {
				preGourp = groupValue;
			}
			AbbCheckListItem item = new AbbCheckListItem(noValue, groupValue, itemValue);
			if (workHourCellIndex > 0) {
				XSSFCell workhour = row.getCell(workHourCellIndex);
				if (workhour != null && __get_cell_value(workhour) != null)
					item.workhour = ((Number)__get_cell_value(workhour)).doubleValue();
			}
			items.put(noValue, item);
			list.add(item);
		}
		
		AbbCheckList clist = new AbbCheckList();
		Set<String> parents = new HashSet<>();
		for (AbbCheckListItem abbCheckListItem : list) {
			if (!parents.contains(abbCheckListItem.parentStr)) {
				AbbCheckListAItem aitem = new AbbCheckListAItem();
				aitem.nm = abbCheckListItem.no.substring(0, abbCheckListItem.no.indexOf('.'));
				aitem.title = abbCheckListItem.parentStr;
				clist.items.add(aitem);
				parents.add(abbCheckListItem.parentStr);
			}
			AbbCheckListAItem aitem = new AbbCheckListAItem();
			aitem.nm = abbCheckListItem.no;
			aitem.title = abbCheckListItem.selfStr;
			aitem.p_ck_nm = abbCheckListItem.no.substring(0, abbCheckListItem.no.indexOf('.'));
			aitem.std_time = abbCheckListItem.workhour;
			clist.items.add(aitem);
		}
		sys.out.writeJson(clist, JsonFormat.full().setIgnoreNull(true));
	}
}
