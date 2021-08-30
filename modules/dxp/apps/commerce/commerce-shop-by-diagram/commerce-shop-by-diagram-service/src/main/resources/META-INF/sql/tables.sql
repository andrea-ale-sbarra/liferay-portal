create table CPDefinitionDiagramEntry (
	CPDefinitionDiagramEntryId LONG not null primary key,
	companyId LONG,
	userId LONG,
	userName VARCHAR(75) null,
	createDate DATE null,
	modifiedDate DATE null,
	CPDefinitionId LONG,
	CPInstanceId LONG,
	CProductId LONG,
	diagram BOOLEAN,
	quantity INTEGER,
	sequence VARCHAR(75) null,
	sku VARCHAR(75) null
);

create table CPDefinitionDiagramPin (
	CPDefinitionDiagramPinId LONG not null primary key,
	companyId LONG,
	userId LONG,
	userName VARCHAR(75) null,
	createDate DATE null,
	modifiedDate DATE null,
	CPDefinitionId LONG,
	positionX DOUBLE,
	positionY DOUBLE,
	sequence VARCHAR(75) null
);

create table CPDefinitionDiagramSetting (
	uuid_ VARCHAR(75) null,
	CPDefinitionDiagramSettingId LONG not null primary key,
	companyId LONG,
	userId LONG,
	userName VARCHAR(75) null,
	createDate DATE null,
	modifiedDate DATE null,
	CPAttachmentFileEntryId LONG,
	CPDefinitionId LONG,
	color VARCHAR(75) null,
	radius DOUBLE,
	type_ VARCHAR(75) null
);