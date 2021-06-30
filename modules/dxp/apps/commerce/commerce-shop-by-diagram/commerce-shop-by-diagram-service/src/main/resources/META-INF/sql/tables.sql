create table CPDefinitionDiagramEntry (
	CPDefinitionDiagramEntryId LONG not null primary key,
	companyId LONG,
	userId LONG,
	userName VARCHAR(75) null,
	createDate DATE null,
	modifiedDate DATE null,
	number_ INTEGER,
	CPInstanceUuid VARCHAR(75) null,
	CProductId LONG,
	CPDefinitionId LONG,
	positionX DOUBLE,
	positionY DOUBLE
);

create table CPDefinitionDiagramSetting (
	uuid_ VARCHAR(75) null,
	CPDefinitionDiagramSettingId LONG not null primary key,
	companyId LONG,
	userId LONG,
	userName VARCHAR(75) null,
	createDate DATE null,
	modifiedDate DATE null,
	CPDefinitionId LONG,
	radius DOUBLE,
	color VARCHAR(75) null
);