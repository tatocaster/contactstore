package com.alexstyl.contactstore

import android.provider.ContactsContract
import com.alexstyl.contactstore.ContactColumn.Events
import com.alexstyl.contactstore.ContactColumn.GroupMemberships
import com.alexstyl.contactstore.ContactColumn.ImAddresses
import com.alexstyl.contactstore.ContactColumn.Image
import com.alexstyl.contactstore.ContactColumn.Mails
import com.alexstyl.contactstore.ContactColumn.Names
import com.alexstyl.contactstore.ContactColumn.Note
import com.alexstyl.contactstore.ContactColumn.Organization
import com.alexstyl.contactstore.ContactColumn.Phones
import com.alexstyl.contactstore.ContactColumn.PostalAddresses
import com.alexstyl.contactstore.ContactColumn.Relations
import com.alexstyl.contactstore.ContactColumn.SipAddresses
import com.alexstyl.contactstore.ContactColumn.WebAddresses

public class MutableContact internal constructor(
    override var contactId: Long = -1L,
    override val lookupKey: LookupKey? = null,
    imageData: ImageData?,
    phones: MutableList<LabeledValue<PhoneNumber>>,
    mails: MutableList<LabeledValue<MailAddress>>,
    events: MutableList<LabeledValue<EventDate>>,
    postalAddresses: MutableList<LabeledValue<PostalAddress>>,
    webAddresses: MutableList<LabeledValue<WebAddress>>,
    note: com.alexstyl.contactstore.Note?,
    override var isStarred: Boolean,
    firstName: String?,
    lastName: String?,
    middleName: String?,
    prefix: String?,
    suffix: String?,
    phoneticFirstName: String?,
    phoneticMiddleName: String?,
    phoneticLastName: String?,
    fullNameStyle: Int,
    phoneticNameStyle: Int,
    nickname: String?,
    organization: String?,
    jobTitle: String?,
    groups: MutableList<GroupMembership>,
    linkedAccountValues: List<LinkedAccountValue>,
    imAddresses: MutableList<LabeledValue<ImAddress>>,
    sipAddresses: MutableList<LabeledValue<SipAddress>>,
    relations: MutableList<LabeledValue<Relation>>,
    override val columns: List<ContactColumn>,
) : Contact {

    override var imageData: ImageData? by readWriteField(Image, imageData)
    override val phones: MutableList<LabeledValue<PhoneNumber>> by requireColumn(Phones, phones)
    override val mails: MutableList<LabeledValue<MailAddress>> by requireColumn(Mails, mails)
    override val events: MutableList<LabeledValue<EventDate>> by requireColumn(Events, events)
    override val postalAddresses: MutableList<LabeledValue<PostalAddress>>
            by requireColumn(PostalAddresses, postalAddresses)
    override val webAddresses: MutableList<LabeledValue<WebAddress>>
            by requireColumn(WebAddresses, webAddresses)
    override val imAddresses: MutableList<LabeledValue<ImAddress>>
            by requireColumn(ImAddresses, imAddresses)
    override val sipAddresses: MutableList<LabeledValue<SipAddress>>
            by requireColumn(SipAddresses, sipAddresses)

    override val linkedAccountValues: List<LinkedAccountValue>
            by requireAnyLinkedAccountColumn(linkedAccountValues)

    override var note: com.alexstyl.contactstore.Note? by readWriteField(Note, note)

    override val groups: MutableList<GroupMembership> by requireColumn(GroupMemberships, groups)

    override val relations: MutableList<LabeledValue<Relation>> by requireColumn(Relations, relations)

    override var organization: String? by readWriteField(Organization, organization)
    override var jobTitle: String? by readWriteField(Organization, jobTitle)
    override var firstName: String? by readWriteField(Names, firstName)
    override var lastName: String? by readWriteField(Names, lastName)
    override var middleName: String? by readWriteField(Names, middleName)
    override var prefix: String? by readWriteField(Names, prefix)
    override var suffix: String? by readWriteField(Names, suffix)
    override var phoneticLastName: String? by readWriteField(Names, phoneticLastName)
    override var phoneticFirstName: String? by readWriteField(Names, phoneticFirstName)
    override var phoneticMiddleName: String? by readWriteField(Names, phoneticMiddleName)
    override var fullNameStyle: Int by readWriteField(Names, fullNameStyle)
    override var phoneticNameStyle: Int by readWriteField(Names, phoneticNameStyle)
    override var nickname: String? by readWriteField(Names, nickname)

    public constructor() : this(
        contactId = -1L,
        imageData = null,
        phones = mutableListOf(),
        mails = mutableListOf(),
        events = mutableListOf(),
        postalAddresses = mutableListOf(),
        webAddresses = mutableListOf(),
        sipAddresses = mutableListOf(),
        relations = mutableListOf(),
        note = null,
        isStarred = false,
        firstName = null,
        lastName = null,
        middleName = null,
        prefix = null,
        suffix = null,
        phoneticFirstName = null,
        phoneticMiddleName = null,
        phoneticLastName = null,
        fullNameStyle = ContactsContract.FullNameStyle.UNDEFINED,
        phoneticNameStyle = ContactsContract.PhoneticNameStyle.UNDEFINED,
        nickname = null,
        organization = null,
        jobTitle = null,
        groups = mutableListOf(),
        linkedAccountValues = emptyList(),
        imAddresses = mutableListOf(),
        columns = standardColumns() // allow editing of all columns for new contacts
    )

    override val displayName: String
        get() = buildString {
            appendWord(buildStringFromNames())

            if (isEmpty()) {
                phoneticFirstName?.let { append(it) }
                phoneticMiddleName?.let { appendWord(it) }
                phoneticLastName?.let { appendWord(it) }
            }
            if (isEmpty()) {
                append(nickname.orEmpty())
            }
            if (isEmpty()) {
                append(organization.orEmpty())
            }
            if (isEmpty()) {
                phones.firstOrNull()?.let { append(it.value.raw) }
            }
            if (isEmpty()) {
                mails.firstOrNull()?.let { append(it.value.raw) }
            }
        }

    override fun equals(other: Any?): Boolean {
        return equalContacts(other as Contact?)
    }

    override fun hashCode(): Int {
        return contactHashCode()
    }

    override fun toString(): String {
        return toFullString()
    }
}