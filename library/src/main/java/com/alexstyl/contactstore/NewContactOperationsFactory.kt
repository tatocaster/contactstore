package com.alexstyl.contactstore

import android.provider.ContactsContract.CommonDataKinds.Email as EmailColumns
import android.provider.ContactsContract.CommonDataKinds.Event as EventColumns
import android.provider.ContactsContract.CommonDataKinds.Im as ImColumns
import android.provider.ContactsContract.CommonDataKinds.Note as NoteColumns
import android.provider.ContactsContract.CommonDataKinds.Organization as OrganizationColumns
import android.provider.ContactsContract.CommonDataKinds.Phone as PhoneColumns
import android.provider.ContactsContract.CommonDataKinds.Photo as PhotoColumns
import android.provider.ContactsContract.CommonDataKinds.Relation as RelationColumns
import android.provider.ContactsContract.CommonDataKinds.StructuredName as NameColumns
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal as PostalColumns
import android.provider.ContactsContract.CommonDataKinds.Website as WebsiteColumns
import android.provider.ContactsContract.CommonDataKinds.SipAddress as SipColumns
import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newInsert
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Contactables
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.FullNameStyle
import android.provider.ContactsContract.PhoneticNameStyle
import android.provider.ContactsContract.RawContacts

internal class NewContactOperationsFactory {
    fun addContactsOperation(contact: MutableContact): List<ContentProviderOperation> {
        return mutableListOf<ContentProviderOperation?>().apply {
            with(contact) {
                add(insertLocalRawAccountOperation(contact))
                add(insertNamesOperation(contact))
                imageData?.run { add(insertPhotoOperation(this)) }

                contact.phones.forEach { add(insertPhoneOperation(it)) }
                contact.mails.forEach { add(insertMailOperation(it)) }
                contact.webAddresses.forEach { add(insertWebOperation(it)) }
                contact.events.forEach { add(insertEventsOperation(it)) }
                contact.postalAddresses.forEach { add(insertPostalOperation(it)) }
                contact.note?.run { add(insertNoteOperation(this)) }
                contact.imAddresses.forEach { add(insertImOperation(it)) }
                contact.sipAddresses.forEach { add(insertSipOperation(it)) }
                contact.relations.forEach { add(insertRelationOperation(it)) }

                if (hasOrganizationDetails(contact)) {
                    add(insertOrganization(contact))
                }
            }
        }
            .filterNotNull()
            .toList()
    }

    private fun hasOrganizationDetails(contact: MutableContact): Boolean {
        return (contact.organization.isNullOrBlank().not()
                || contact.jobTitle.isNullOrBlank().not())
    }

    private fun insertOrganization(contact: Contact): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, OrganizationColumns.CONTENT_ITEM_TYPE)
            .withValue(OrganizationColumns.TITLE, contact.jobTitle)
            .withValue(OrganizationColumns.COMPANY, contact.organization)
            .build()
    }

    private fun insertWebOperation(labeledValue: LabeledValue<WebAddress>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, WebsiteColumns.CONTENT_ITEM_TYPE)
            .withValue(WebsiteColumns.URL, labeledValue.value.raw)
            .withWebsiteLabel(labeledValue.label)
            .build()
    }

    private fun insertImOperation(labeledValue: LabeledValue<ImAddress>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, ImColumns.CONTENT_ITEM_TYPE)
            .withValue(ImColumns.DATA, labeledValue.value.raw)
            .withValue(ImColumns.CUSTOM_PROTOCOL, labeledValue.value.protocol)
            .withImLabel(labeledValue.label)
            .build()
    }

    private fun insertSipOperation(labeledValue: LabeledValue<SipAddress>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, SipColumns.CONTENT_ITEM_TYPE)
            .withValue(SipColumns.SIP_ADDRESS, labeledValue.value.raw)
            .withSipLabel(labeledValue.label)
            .build()
    }

    private fun insertNoteOperation(note: Note): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, NoteColumns.CONTENT_ITEM_TYPE)
            .withValue(NoteColumns.NOTE, note.raw)
            .build()
    }

    private fun insertPhotoOperation(imageData: ImageData): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, PhotoColumns.CONTENT_ITEM_TYPE)
            .withValue(PhotoColumns.PHOTO, imageData.raw)
            .build()
    }

    private fun insertPhoneOperation(labeledValue: LabeledValue<PhoneNumber>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, PhoneColumns.CONTENT_ITEM_TYPE)
            .withValue(PhoneColumns.NUMBER, labeledValue.value.raw)
            .withPhoneLabel(labeledValue.label)
            .build()
    }

    private fun insertMailOperation(labeledValue: LabeledValue<MailAddress>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, EmailColumns.CONTENT_ITEM_TYPE)
            .withValue(EmailColumns.ADDRESS, labeledValue.value.raw)
            .withMailLabel(labeledValue.label)
            .build()
    }

    private fun insertEventsOperation(labeledValue: LabeledValue<EventDate>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, EventColumns.CONTENT_ITEM_TYPE)
            .withValue(EmailColumns.ADDRESS, sqlString(labeledValue.value))
            .withEventLabel(labeledValue.label)
            .build()
    }

    private fun insertPostalOperation(labeledValue: LabeledValue<PostalAddress>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, PostalColumns.CONTENT_ITEM_TYPE)
            .withValue(PostalColumns.CITY, labeledValue.value.city)
            .withValue(PostalColumns.COUNTRY, labeledValue.value.country)
            .withValue(PostalColumns.NEIGHBORHOOD, labeledValue.value.neighborhood)
            .withValue(PostalColumns.POBOX, labeledValue.value.poBox)
            .withValue(PostalColumns.POSTCODE, labeledValue.value.postCode)
            .withValue(PostalColumns.REGION, labeledValue.value.region)
            .withValue(PostalColumns.STREET, labeledValue.value.street)
            .withPostalAddressLabel(labeledValue.label)
            .build()
    }

    private fun insertRelationOperation(labeledValue: LabeledValue<Relation>): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, RelationColumns.CONTENT_ITEM_TYPE)
            .withValue(RelationColumns.NAME, labeledValue.value.name)
            .withRelationLabel(labeledValue.label)
            .build()
    }

    private fun insertLocalRawAccountOperation(contact: MutableContact): ContentProviderOperation {
        return newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_TYPE, null)
            .withValue(RawContacts.ACCOUNT_NAME, null)
            .withValue(Data.STARRED, boolToString(contact.isStarred))
            .build()
    }

    private fun boolToString(bool: Boolean): String {
        return if (bool) {
            "1"
        } else {
            "0"
        }
    }

    private fun insertNamesOperation(contact: Contact): ContentProviderOperation {
        return newInsert(Data.CONTENT_URI)
            .withValueBackReference(Data.RAW_CONTACT_ID, NEW_CONTACT_INDEX)
            .withValue(Data.MIMETYPE, NameColumns.CONTENT_ITEM_TYPE)
            .withValue(NameColumns.GIVEN_NAME, contact.firstName)
            .withValue(NameColumns.FAMILY_NAME, contact.lastName)
            .withValue(NameColumns.MIDDLE_NAME, contact.middleName)
            .withValue(NameColumns.SUFFIX, contact.suffix)
            .withValue(NameColumns.PREFIX, contact.prefix)
            .let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    it.withValue(NameColumns.FULL_NAME_STYLE, FullNameStyle.UNDEFINED)
                } else {
                    it
                }
            }
            .withValue(NameColumns.PHONETIC_GIVEN_NAME, contact.phoneticFirstName)
            .withValue(NameColumns.PHONETIC_FAMILY_NAME, contact.phoneticLastName)
            .withValue(NameColumns.PHONETIC_MIDDLE_NAME, contact.phoneticMiddleName)
            .withValue(NameColumns.PHONETIC_NAME_STYLE, PhoneticNameStyle.UNDEFINED)
            .build()
    }

    private fun ContentProviderOperation.Builder.withPhoneLabel(
        label: Label
    ): ContentProviderOperation.Builder {
        return when (label) {
            Label.PhoneNumberMobile -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_MOBILE
            )
            Label.LocationHome -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_HOME
            )
            Label.LocationWork -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_WORK
            )
            Label.PhoneNumberPager -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_PAGER
            )
            Label.PhoneNumberCar -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_CAR
            )
            Label.PhoneNumberFaxWork -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_FAX_WORK
            )
            Label.PhoneNumberFaxHome -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_FAX_HOME
            )
            Label.PhoneNumberCallback -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_CALLBACK
            )
            Label.PhoneNumberCompanyMain -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_COMPANY_MAIN
            )
            Label.PhoneNumberIsdn -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_ISDN
            )
            Label.Main -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_MAIN
            )
            Label.PhoneNumberOtherFax -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_OTHER_FAX
            )
            Label.PhoneNumberRadio -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_RADIO
            )
            Label.PhoneNumberTelex -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_TELEX
            )
            Label.PhoneNumberTtyTdd -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_TTY_TDD
            )
            Label.PhoneNumberWorkPager -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_WORK_PAGER
            )
            Label.PhoneNumberWorkMobile -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_WORK_MOBILE
            )
            Label.PhoneNumberAssistant -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_ASSISTANT
            )
            Label.PhoneNumberMms -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_MMS
            )
            Label.Other -> withValue(
                Contactables.TYPE,
                PhoneColumns.TYPE_OTHER
            )
            is Label.Custom -> {
                withValue(
                    Contactables.TYPE,
                    Contactables.TYPE_CUSTOM
                )
                    .withValue(Contactables.LABEL, label.label)
            }
            else -> error("Unsuported Phone label $label")
        }
    }

    private fun ContentProviderOperation.Builder.withMailLabel(
        label: Label
    ): ContentProviderOperation.Builder {
        return when (label) {
            Label.LocationHome -> withValue(
                Contactables.TYPE,
                EmailColumns.TYPE_HOME
            )
            Label.LocationWork -> withValue(
                Contactables.TYPE,
                EmailColumns.TYPE_WORK
            )
            Label.Other -> withValue(
                Contactables.TYPE,
                EmailColumns.TYPE_OTHER
            )
            Label.PhoneNumberMobile -> withValue(
                Contactables.TYPE,
                EmailColumns.TYPE_MOBILE
            )
            is Label.Custom -> {
                withValue(
                    Contactables.TYPE,
                    Contactables.TYPE_CUSTOM
                )
                    .withValue(Contactables.LABEL, label.label)
            }
            else -> error("Unsupported Mail Label $label")
        }
    }

    private fun ContentProviderOperation.Builder.withWebsiteLabel(
        label: Label
    ): ContentProviderOperation.Builder {
        return when (label) {
            Label.WebsiteBlog -> withValue(Contactables.TYPE, WebsiteColumns.TYPE_BLOG)
            Label.WebsiteFtp -> withValue(Contactables.TYPE, WebsiteColumns.TYPE_FTP)
            Label.LocationHome -> withValue(Contactables.TYPE, WebsiteColumns.TYPE_HOME)
            Label.WebsiteHomePage -> withValue(Contactables.TYPE, WebsiteColumns.TYPE_HOMEPAGE)
            Label.Other -> withValue(Contactables.TYPE, WebsiteColumns.TYPE_OTHER)
            Label.LocationWork -> withValue(Contactables.TYPE, WebsiteColumns.TYPE_WORK)
            Label.WebsiteProfile -> withValue(Contactables.TYPE, WebsiteColumns.TYPE_PROFILE)
            is Label.Custom -> {
                withValue(Contactables.TYPE, Contactables.TYPE_CUSTOM)
                    .withValue(Contactables.LABEL, label.label)
            }
            else -> error("Unsupported Website Label $label")
        }
    }


    private fun ContentProviderOperation.Builder.withPostalAddressLabel(
        label: Label
    ): ContentProviderOperation.Builder {
        return when (label) {
            Label.LocationHome -> withValue(
                Contactables.TYPE,
                PostalColumns.TYPE_HOME
            )
            Label.LocationWork -> withValue(
                Contactables.TYPE,
                PostalColumns.TYPE_WORK
            )
            Label.Other -> withValue(
                Contactables.TYPE,
                PostalColumns.TYPE_OTHER
            )
            is Label.Custom -> {
                withValue(
                    Contactables.TYPE,
                    Contactables.TYPE_CUSTOM
                )
                    .withValue(Contactables.LABEL, label.label)
            }
            else -> error("Unsupported Postal Label $label")
        }
    }

    private fun ContentProviderOperation.Builder.withImLabel(
        label: Label
    ): ContentProviderOperation.Builder {
        return when (label) {
            Label.LocationHome -> withValue(
                Contactables.TYPE,
                ImColumns.TYPE_HOME
            )
            Label.LocationWork -> withValue(
                Contactables.TYPE,
                ImColumns.TYPE_WORK
            )
            Label.Other -> withValue(
                Contactables.TYPE,
                ImColumns.TYPE_OTHER
            )
            is Label.Custom -> {
                withValue(
                    Contactables.TYPE,
                    Contactables.TYPE_CUSTOM
                )
                    .withValue(Contactables.LABEL, label.label)
            }
            else -> error("Unsupported Im Label $label")
        }
    }
    private fun ContentProviderOperation.Builder.withEventLabel(
        label: Label
    ): ContentProviderOperation.Builder {
        return when (label) {
            Label.DateAnniversary -> withValue(
                Contactables.TYPE,
                EventColumns.TYPE_ANNIVERSARY
            )
            Label.DateBirthday -> withValue(
                Contactables.TYPE,
                EventColumns.TYPE_BIRTHDAY
            )
            Label.Other -> withValue(
                Contactables.TYPE,
                EventColumns.TYPE_OTHER
            )
            is Label.Custom -> {
                withValue(
                    Contactables.TYPE,
                    Contactables.TYPE_CUSTOM
                )
                    .withValue(Contactables.LABEL, label.label)
            }
            else -> error("Unsupported Event Label $label")
        }
    }

    private companion object {
        const val NEW_CONTACT_INDEX = 0
    }
}

internal fun ContentProviderOperation.Builder.withRelationLabel(
    label: Label
): ContentProviderOperation.Builder {
    return when (label) {
        Label.PhoneNumberAssistant -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_ASSISTANT
        )
        Label.RelationBrother -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_BROTHER
        )
        Label.RelationDomesticPartner -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_DOMESTIC_PARTNER
        )
        Label.RelationChild -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_CHILD
        )
        Label.RelationFather -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_FATHER
        )
        Label.RelationMother -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_MOTHER
        )
        Label.RelationManager -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_MANAGER
        )
        Label.RelationFriend -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_FRIEND
        )
        Label.RelationParent -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_PARENT
        )
        Label.RelationPartner -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_PARTNER
        )
        Label.RelationReferredBy -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_REFERRED_BY
        )
        Label.RelationSister -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_SISTER
        )
        Label.RelationSpouse -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_SPOUSE
        )
        Label.RelationRelative -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_RELATIVE
        )
        Label.Other -> withValue(
            Contactables.TYPE,
            RelationColumns.TYPE_CHILD
        )
        is Label.Custom -> {
            withValue(
                Contactables.TYPE,
                Contactables.TYPE_CUSTOM
            )
                .withValue(Contactables.LABEL, label.label)
        }
        else -> error("Unsupported Postal Label $label")
    }
}

internal fun ContentProviderOperation.Builder.withSipLabel(
    label: Label
): ContentProviderOperation.Builder {
    return when (label) {
        Label.LocationHome -> withValue(
            Contactables.TYPE,
            SipColumns.TYPE_HOME
        )
        Label.Other -> withValue(
            Contactables.TYPE,
            SipColumns.TYPE_OTHER
        )
        Label.LocationWork -> withValue(
            Contactables.TYPE,
            SipColumns.TYPE_WORK
        )
        is Label.Custom -> {
            withValue(
                Contactables.TYPE,
                Contactables.TYPE_CUSTOM
            )
                .withValue(Contactables.LABEL, label.label)
        }
        else -> error("Unsupported Im Label $label")
    }
}
