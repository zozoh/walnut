package org.nutz.dao.impl.jdbc.oracle;

import org.nutz.dao.DB;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.LinkField;
import org.nutz.dao.entity.MappingField;
import org.nutz.dao.entity.PkType;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.impl.jdbc.AbstractJdbcExpert;
import org.nutz.dao.impl.jdbc.BlobValueAdaptor2;
import org.nutz.dao.impl.jdbc.ClobValueAdapter2;
import org.nutz.dao.jdbc.JdbcExpertConfigFile;
import org.nutz.dao.jdbc.Jdbcs;
import org.nutz.dao.jdbc.ValueAdaptor;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.PItem;
import org.nutz.dao.sql.Pojo;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Pojos;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutMap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OracleJdbcExpert extends AbstractJdbcExpert {

	//指定oracle表空间的TableMeta' key
    private static final String META_TABLESPACE = "oracle-tablespace";
	
	//oracle 创建表时指定表空间的默认sql
    private static String CTS = "tablespace %s\n" +
            "  pctfree 10\n" +
            "  initrans 1\n" +
            "  maxtrans 255\n" +
            "  storage\n" +
            "  (\n" +
            "    initial 64K\n" +
            "    minextents 1\n" +
            "    maxextents unlimited\n" +
            "  )";

    private static String CSEQ = "CREATE SEQUENCE ${T}_${F}_SEQ  MINVALUE 1"
                                 + " MAXVALUE 999999999999 INCREMENT BY 1 START"
                                 + " WITH 1 CACHE 20 NOORDER  NOCYCLE";
    private static String DSEQ = "DROP SEQUENCE ${T}_${F}_SEQ";

    private static String CTRI = "create or replace trigger ${T}_${F}_ST"
                                 + " BEFORE INSERT ON ${T}"
                                 + " FOR EACH ROW"
                                 + " BEGIN "
                                 + " IF :new.${F} IS NULL THEN"
                                 + " SELECT ${T}_${F}_seq.nextval into :new.${F} FROM dual;"
                                 + " END IF;"
                                 + " END ${T}_${F}_ST;";

    protected boolean ignoreOneRowPager;

    public OracleJdbcExpert(JdbcExpertConfigFile conf) {
        super(conf);
    }

    public ValueAdaptor getAdaptor(MappingField ef) {
        Mirror<?> mirror = ef.getMirror();
        if (mirror.isBoolean())
            return new OracleBooleanAdaptor();
        if (mirror.isOf(Clob.class))
            return new ClobValueAdapter2(Jdbcs.getFilePool());
        if (mirror.isOf(Blob.class))
            return new BlobValueAdaptor2(Jdbcs.getFilePool());
        return super.getAdaptor(ef);
    }

    public boolean createEntity(Dao dao, Entity<?> en) {
        StringBuilder sb = new StringBuilder("CREATE TABLE " + en.getTableName() + "(");
        // 创建字段
        for (MappingField mf : en.getMappingFields()) {
            if (mf.isReadonly())
                continue;
            sb.append('\n').append(mf.getColumnNameInSql());
            sb.append(' ').append(evalFieldType(mf));
            // 非主键的 @Name，应该加入唯一性约束
            if (mf.isName() && en.getPkType() != PkType.NAME) {
                sb.append(" NOT NULL UNIQUE");
            }
            // 普通字段
            else {
                if (mf.isPk() && en.getPks().size() == 1)
                    sb.append(" primary key ");
                if (mf.isNotNull())
                    sb.append(" NOT NULL");
                if (mf.hasDefaultValue() && mf.getColumnType() != ColType.BOOLEAN)
                    addDefaultValue(sb, mf);
                if (mf.isUnsigned() && mf.getColumnType() != ColType.BOOLEAN) // 有点暴力
                    sb.append(" Check ( ").append(mf.getColumnNameInSql()).append(" >= 0)");
            }
            sb.append(',');
        }

        // 结束表字段设置
        sb.setCharAt(sb.length() - 1, ')');

		//指定表空间
        if(en.hasMeta(META_TABLESPACE)){
            sb.append(String.format(CTS, en.getMeta(META_TABLESPACE)));
        }

        List<Sql> sqls = new ArrayList<Sql>();
        sqls.add(Sqls.create(sb.toString()));

        // 创建复合主键
        List<MappingField> pks = en.getPks();
        if (pks.size() > 1) {
            StringBuilder pkNames = new StringBuilder();
            for (MappingField pk : pks) {
                pkNames.append(pk.getColumnName()).append(',');
            }
            pkNames.setLength(pkNames.length() - 1);

            String pkNames2 = makePksName(en);

            String sql = String.format("alter table %s add constraint primary_key_%s primary key (%s)",
                                       en.getTableName(),
                                       pkNames2,
                                       pkNames);
            sqls.add(Sqls.create(sql));
        }
        // // 处理非主键unique
        // for (MappingField mf : en.getMappingFields()) {
        // if(!mf.isPk())
        // continue;
        // String sql =
        // gSQL("alter table ${T} add constraint unique_key_${F} unique (${F});",
        // en.getTableName(),mf.getColumnName());
        // sqls.add(Sqls.create(sql));
        // }
        // 处理AutoIncreasement
        for (MappingField mf : en.getMappingFields()) {
            if (!mf.isAutoIncreasement())
                continue;
            // 序列
            sqls.add(Sqls.create(gSQL(CSEQ, en.getTableName(), mf.getColumnName())));
            // 触发器
            sqls.add(Sqls.create(gSQL(CTRI, en.getTableName(), mf.getColumnName())));
        }

        // 创建索引
        sqls.addAll(createIndexs(en));

        // TODO 详细处理Clob
        // TODO 详细处理Blob

        // 执行创建语句
        dao.execute(sqls.toArray(new Sql[sqls.size()]));
        // 创建关联表
        createRelation(dao, en);
        // 添加注释(表注释与字段注释)
        addComment(dao, en);

        return true;
    }

    public void formatQuery(Pojo pojo) {
        Pager pager = pojo.getContext().getPager();
        // 需要进行分页
        if (null != pager && pager.getPageNumber() > 0) {
            if (ignoreOneRowPager && pager.getPageNumber() == 1 && pager.getPageSize() == 1)
                return;
            pojo.insertFirst(Pojos.Items.wrap("SELECT * FROM (SELECT T.*, ROWNUM RN FROM ("));
            pojo.append(Pojos.Items.wrapf(") T WHERE ROWNUM <= %d) WHERE RN > %d",
                                          pager.getOffset() + pager.getPageSize(),
                                          pager.getOffset()));
        }
    }

    @Override
    public void formatQuery(Sql sql) {
        Pager pager = sql.getContext().getPager();
        // 需要进行分页
        if (null != pager && pager.getPageNumber() > 0) {
            if (ignoreOneRowPager && pager.getPageNumber() == 1 && pager.getPageSize() == 1)
                return;
            String pre = "SELECT * FROM (SELECT T.*, ROWNUM RN FROM (";
            String last = String.format(") T WHERE ROWNUM <= %d) WHERE RN > %d",
                                        pager.getOffset() + pager.getPageSize(),
                                        pager.getOffset());
            sql.setSourceSql(pre + sql.getSourceSql() + last);
        }
    }

    public String getDatabaseType() {
        return DB.ORACLE.name();
    }

    public String evalFieldType(MappingField mf) {
        if (mf.getCustomDbType() != null)
            return mf.getCustomDbType();
        switch (mf.getColumnType()) {
        case BOOLEAN:
            if (mf.hasDefaultValue())
                return "char(1) DEFAULT '"+getDefaultValue(mf)+"' check (" + mf.getColumnNameInSql() + " in(0,1))";
            return "char(1) check (" + mf.getColumnNameInSql() + " in(0,1))";
        case TEXT:
            return "CLOB";
        case VARCHAR:
            return "VARCHAR2(" + mf.getWidth() + ")";
        case INT:
            // 用户自定义了宽度
            if (mf.getWidth() > 0)
                return "NUMBER(" + mf.getWidth() + ")";
            // 用数据库的默认宽度
            return "NUMBER";

        case FLOAT:
            // 用户自定义了精度
            if (mf.getWidth() > 0 && mf.getPrecision() > 0) {
                return "NUMBER(" + mf.getWidth() + "," + mf.getPrecision() + ")";
            }
            // 用默认精度
            if (mf.getMirror().isDouble())
                return "NUMBER(15,10)";
            return "NUMBER";
        case TIME:
        case DATETIME:
        case DATE:
            return "DATE";
        default:
            return super.evalFieldType(mf);
        }
    }

    @Override
    protected String createResultSetMetaSql(Entity<?> en) {
        return "select * from " + en.getViewName() + " where rownum <= 1";
    }

    @Override
    public boolean dropEntity(Dao dao, Entity<?> en) {
        if (super.dropEntity(dao, en)) {
            if (en.getPks().isEmpty())
                return true;
            List<Sql> sqls = new ArrayList<Sql>();
            for (MappingField pk : en.getPks()) {
                if (pk.isAutoIncreasement()) {
                    String sql = gSQL(DSEQ, en.getTableName(), pk.getColumnName());
                    sqls.add(Sqls.create(sql));
                }
            }
            try {
                dao.execute(sqls.toArray(new Sql[sqls.size()]));
                return true;
            }
            catch (Exception e) {}
        }
        return false;
    }

    public boolean isSupportAutoIncrement() {
        return false;
    }
    
    public boolean addColumnNeedColumn() {
        return false;
    }
    
    public boolean supportTimestampDefault() {
        return false;
    }
    
    public String wrapKeyword(String columnName, boolean force) {
        if (force || keywords.contains(columnName.toUpperCase()))
            return "\"" + columnName + "\"";
        return null;
    }
    
    // https://docs.oracle.com/cd/B12037_01/server.101/b10755/statviews_1061.htm
    public List<String> getIndexNames(Entity<?> en, Connection conn) throws SQLException {
        List<String> names = new ArrayList<String>();
        String showIndexs = "SELECT * FROM user_indexes WHERE table_name='" + en.getTableName()+"'";
        PreparedStatement ppstat = conn.prepareStatement(showIndexs);
        ResultSet rest = ppstat.executeQuery();
        while (rest.next()) {
            String index = rest.getString(2);
            names.add(index);
        }
        return names;
    }

    public void setupProperties(NutMap conf) {
        super.setupProperties(conf);
        this.ignoreOneRowPager = conf.getBoolean("nutz.dao.jdbc.oracle.ignoreOneRowPager", false);
    }
    
    @Override
    public PItem formatLeftJoinLink(Object obj, LinkField lnk, Entity<?> en) {
    	Entity<?> lnkEntity = lnk.getLinkedEntity();
        String linkName = safeTableName(lnk.getName());
        String LJ = String.format("LEFT JOIN %s %s ON %s.%s = %s.%s",
                                  lnkEntity.getTableName(),
                                  linkName,
                                  en.getTableName(),
                                  lnk.getHostField().getColumnNameInSql(),
                                  linkName,
                                  lnk.getLinkedField().getColumnNameInSql());
    	return Pojos.Items.wrap(LJ);
    }
}
