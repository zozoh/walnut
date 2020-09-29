package org.nutz.walnut.ext.abb.hdl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class abb_ckimp implements JvmHdl {

	private static final Log log = Logs.get();

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
		_exec(sys, hc, wb);
		wb.close();

		wb.close();
	}

	protected void _exec(WnSystem sys, JvmHdlContext hc, XSSFWorkbook wb) {
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
			aitem.p_ck_nm = abbCheckListItem.parentStr.split(" ", 2)[1];
			clist.items.add(aitem);
		}
		sys.out.writeJson(clist, JsonFormat.full());
	}

	@SuppressWarnings("deprecation")
	private Object __get_cell_value(Cell cell) {
		if (cell == null)
			return null;
		CellType cellType = cell.getCellTypeEnum();
		switch (cellType) {
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue();
			}
			double n = cell.getNumericCellValue();
			long ni = (long) n;
			if (ni == n)
				return ni;
			return n;
		case STRING:
			return Strings.trim(cell.getStringCellValue());
		case FORMULA:
			return cell.getCellFormula();
		case BLANK:
			return null;
		case BOOLEAN:
			return cell.getBooleanCellValue();
		case ERROR:
			return cell.getErrorCellValue();
		default:
			break;
		}
		throw Er.create("e.sheet.xls.unknownCellType", cellType);
	}
}
