package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiErrSum;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.erm.IcsReplyERM;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.List;

public class ERMLoader implements EdiMsgLoader<IcsReplyERM> {

	@Override
	public Class<IcsReplyERM> getResultType() {
		return IcsReplyERM.class;
	}

	@Override
	public IcsReplyERM load(EdiMessage msg) {
		IcsReplyERM re = new IcsReplyERM();

		EdiSegmentFinder finder = msg.getFinder();
		NutMap bean = new NutMap();
		List<EdiSegment> segs;

		/*
		 * 定位到 BGM 报文行，解析 Version 和 FuncCode
		 * BGM+963:::ERM+CCF_FKK363E_1_ACR_1:1+11'
		 */
		IcsLoaderHelper.parseBgmSeg(re, finder);

		/*
		 * 解析 FTX 报文行
		 * FTX+ACX+++ACR'
		 * FTX+ACX+++SCR'
		 */
		finder.reset();
		boolean find = finder.moveToUtil("FTX", true, "NAD", "RFF", "DTM", "ERP", "CNT", "UNT");
		boolean ermError = false;
		if (find) {
			segs = finder.nextAll(true, "FTX");
			for (EdiSegment seg : segs) {
				bean.clear();
				seg.fillBean(bean, null, "subjectCode", null, null, "textValue");
				if (!bean.is("subjectCode", "ACX")) {
					continue;
				}

				String ermType = Strings.sNull(bean.getString("textValue")).trim();
				if (IcsReplyERM.ERM_TYPE_ACR.equals(ermType)) {
					ermError = true;
					re.setErmType(ermType);
				} else if (IcsReplyERM.ERM_TYPE_SCR.equals(ermType)) {
					ermError = true;
					re.setErmType(ermType);
				}
			}
		}
		re.setErmError(ermError);

		// 解析 NAD 报文行
		finder.reset();
		find = finder.moveToUtil("NAD", true, "RFF", "DTM", "ERP", "CNT", "UNT");
		if (find) {
			segs = finder.nextAll(true, "NAD");
			for (EdiSegment seg : segs) {
				bean.clear();
				seg.fillBean(bean, null, "funcCode", "icsSiteId,,agencyCode");
				if (bean.is("funcCode", "MR")) {
					re.setIcsSiteId(bean.getString("icsSiteId"));
				}
			}
		}

		/*
		 * 解析 RFF 报文行
		 * RFF+AAY:mpqwwny2930i7e7jlv-0::1'
		 */
		finder.reset();
		find = finder.moveToUtil("RFF", true, "DTM", "ERP", "CNT", "UNT");
		if (find) {
			segs = finder.nextAll(true, "RFF");
			for (EdiSegment seg : segs) {
				bean.clear();
				seg.fillBean(bean, null, "refType,refId,,refVer");
				if (ermError && bean.is("refType", "AAY")) {
					String refId = Strings.sNull(bean.getString("refId")).trim();
					if (Strings.isNotBlank(refId)) {
						re.setRefId(refId);
						re.setRefVer(bean.getInt("refVer"));
					}
				}
			}
		}

		// 解析 DTM 报文行: DTM+310:20260530015542:204'
		finder.reset();
		find = finder.moveToUtil("DTM", true, "ERP", "CNT", "UNT");
		if (find) {
			segs = finder.nextAll(true, "DTM");
			for (EdiSegment seg : segs) {
				bean.clear();
				seg.fillBean(bean, null, "funcCode,msgRcvTime,");
				if (bean.is("funcCode", "310")) {
					re.setMsgRcvTime(bean.getString("msgRcvTime"));
					break;
				}
			}
		}

		// 解析错误信息: 解析 SG4: ERP-ERC-FTX 报文组, 以及 CNT+55:${CountNum}' 报文行
		EdiErrSum ediErrSum = IcsLoaderHelper.collectEdiErrSum(finder);
		re.setErrs(ediErrSum.getErrs());
		re.setErrCount(ediErrSum.getErrCount());
		re.setSuccess(ediErrSum.isSuccess());

		// 返回解析结果
		return re;
	}
}
