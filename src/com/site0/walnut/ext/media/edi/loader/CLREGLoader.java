package com.site0.walnut.ext.media.edi.loader;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.ori.EdiOriCLREG;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.util.Ws;

public class CLREGLoader implements EdiMsgLoader<EdiOriCLREG> {

    @Override
    public Class<EdiOriCLREG> getResultType() {
        return EdiOriCLREG.class;
    }

    @Override
    public EdiOriCLREG load(EdiMessage msg) {
        EdiOriCLREG re = new EdiOriCLREG();
        EdiSegmentFinder finder = msg.getFinder();
        EdiSegment _seg;
        List<EdiSegment> _seg_list;
        NutMap bean = new NutMap();

        // ..............................................
        // #
        // # 这里是报文的正文
        // #
        // BGM+101:::CLREG+${CLREG.SenderReference}:${CLREG.SenderReferenceVersion?1}+9'
        _seg = finder.next("BGM");
        _seg.fillBean(re.CLREG, null, null, "SenderReference,SenderReferenceVersion");

        // ..............................................
        // 获取 BP Type
        _seg = finder.next("RFF");
        bean.clear();
        _seg.fillBean(bean, null, "mode,type");
        // ${#if "BP.type":"ORG","BP.reg_num_primary":"![BLANK]"}
        // RFF+ABN:${BP.reg_num_primary}'
        if (bean.is("mode", "ABN")) {
            re.BP.put("type", "ORG");
            re.BP.put("reg_num_primary", bean.get("type"));
        }
        // ${#else-if "BP.type":"ORG"}
        // RFF+AQU:ORG'
        // ${#else-if "BP.type":"IND"}
        // RFF+AQU:IND'
        else if (bean.is("mode", "AQU")) {
            re.BP.put("type", bean.get("type"));
        }
        // FTX+AFM+++${BP.title}:${BP.name_first}:${BP.name_second}:${BP.name_family}:${BP.suffix}'
        if (re.BP.is("type", "IND")) {
            _seg = finder.next("FTX");
            _seg.fillBean(re.BP,
                          null,
                          null,
                          null,
                          null,
                          "title,name_first,name_second,name_family,suffix");
        }
        // ${#end}

        // ..............................................
        // #
        // # 联系人，目的
        // #
        // FTX+CNP+++${C.contact_name}:${purpose?EXPORT}'
        _seg = finder.next("FTX");
        _seg.fillBean(bean.reset(), null, null, null, null, "contact_name,purpose");
        re.purpose = bean.getString("purpose");
        if (bean.has("contact_name")) {
            re.C.put("contact_name", bean.getString("contact_name"));
        }

        // #
        // # BP 的组织识别名称，如果是 ABN 组织则无需这段信息
        // #
        // ${#if "BP.type":"ORG","BP.reg_num_primary":"[BLANK]"}
        // TDT+1'
        // LOC+ZZZ+:::${BP.name}'
        if (re.BP.is("type", "ORG") && !re.BP.has("reg_num_primary")) {
            _seg = finder.next("LOC");
            _seg.fillBean(re.BP, null, null, ",,,name");
        }
        // ${#end}

        // ..............................................
        // #
        // # 地址信息
        // # 如果是组织且有 ABN 但是没有 CAC，不发这条报文
        // #
        // ${#if not
        // "BP.type":"ORG","BP.reg_num_primary":"![BLANK]","BP.reg_num_second":"[BLANK]"}
        // FTX+ATY+BA++${A.address_1}:${A.address_2?}:${A.city}:${A.postcode}:${A.state}+${A.country}'
        // ${#end}
        // #
        // # 联系人信息 - after_hours_phone
        // #
        // ${#if not "C.after_hours_phone":"[BLANK]"}
        // FTX+CAT+AP+${C.after_hours_phone_prefix}+${C.after_hours_phone}:${C.after_hours_phone_comments}'
        // ${#end}
        // #
        // # 联系人信息 - mobile
        // # ERC+CL0467:6:95
        // # PREFIX CODE ONLY ALLOWED WITH AP, FA OR BP CONTACT ADDRESS
        // #
        // ${#if not "C.mobile":"[BLANK]"}
        // FTX+CAT+MO++${C.mobile}:${C.mobile_comments}'
        // ${#end}
        // #
        // # 联系人信息 - Email
        // #
        // ${#if not "C.email":"[BLANK]"}
        // FTX+CAT+EA++${C.email}'
        // ${#end}
        // #
        // # 联系人地址
        // #
        // ${#if not CA:"[BLANK]"}
        // FTX+CAT+BA++${CA.address_1}:${CA.address_2?}:${CA.city}:${CA.postcode}:${CA.state}+${CA.country}'
        // ${#end}
        _seg_list = finder.nextAllUtilNoMatch(true, "FTX");
        for (EdiSegment seg : _seg_list) {
            seg.fillBean(bean.reset(), null, "mode", "type");
            // ${#if not
            // "BP.type":"ORG","BP.reg_num_primary":"![BLANK]","BP.reg_num_second":"[BLANK]"}
            // FTX+ATY+BA++${A.address_1}:${A.address_2?}:${A.city}:${A.postcode}:${A.state}+${A.country}'
            // ${#end}
            if (bean.is("type", "BA") && bean.is("mode", "ATY")) {
                seg.fillBean(re.A,
                             null,
                             null,
                             null,
                             null,
                             "address_1,address_2,city,postcode,state,country");
            }
            // FTX+CAT+AP+${C.after_hours_phone_prefix}+${C.after_hours_phone}:${C.after_hours_phone_comments}'
            else if (bean.is("type", "AP")) {
                seg.fillBean(re.C,
                             null,
                             null,
                             null,
                             "after_hours_phone_prefix,after_hours_phone,after_hours_phone_comments");
            }
            // FTX+CAT+MO++${C.mobile}:${C.mobile_comments}'
            else if (bean.is("type", "MO")) {
                seg.fillBean(re.C, null, null, null, null, "mobile,mobile_comments");
            }
            // FTX+CAT+EA++${C.email}'
            else if (bean.is("type", "EA")) {
                seg.fillBean(re.C, null, null, null, null, "email");
            }
            // FTX+CAT+BA++${CA.address_1}:${CA.address_2?}:${CA.city}:${CA.postcode}:${CA.state}+${CA.country}'
            else if (bean.is("type", "BA") && bean.is("mode", "CAT")) {
                seg.fillBean(re.CA,
                             null,
                             null,
                             null,
                             null,
                             "address_1,address_2,city,postcode,state,country");
            }
        }

        // ..............................................
        // #
        // # 采用尺寸信息来传递角色信息
        // #
        // ${#loop roleName : clientRoles}
        // MEA+RN+:::${roleName}'
        // ${#end}
        _seg_list = finder.nextAllUtilNoMatch(true, "MEA");
        List<String> roles = new ArrayList<>(_seg_list.size());
        for (EdiSegment seg : _seg_list) {
            bean.clear();
            seg.fillBean(bean.reset(), null, null, ",,,roleName");
            String roleName = bean.getString("roleName");
            if (!Ws.isBlank(roleName)) {
                roles.add(roleName);
            }
        }
        re.clientRoles = roles.toArray(new String[roles.size()]);

        // ..............................................
        // #
        // # 个人的信息
        // #
        // ${#if "BP.type":"IND"}
        // AUT+GE+${BP.gender}'
        // DTM+329:${BP.dob<date:yyyyMMdd>}:102'
        // ${#end}

        if (re.BP.is("type", "IND")) {
            _seg = finder.next("AUT");
            _seg.fillBean(re.BP, null, null, "gender");
            _seg = finder.next("DTM");
            _seg.fillBean(re.BP, null, ",dob");
        }

        // UNT+14+1'
        // UNZ+${_H.MessageCount?1}+${_H.ControlReferenceNumber}'
        return re;
    }

}
