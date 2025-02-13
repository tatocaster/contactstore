@file:JvmName("ContactKt")

package com.alexstyl.contactstore

import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Contacts.Photo
import android.provider.ContactsContract.FullNameStyle
import android.provider.ContactsContract.PhoneticNameStyle
import com.alexstyl.contactstore.ContactColumn.Events
import com.alexstyl.contactstore.ContactColumn.GroupMemberships
import com.alexstyl.contactstore.ContactColumn.ImAddresses
import com.alexstyl.contactstore.ContactColumn.Image
import com.alexstyl.contactstore.ContactColumn.LinkedAccountValues
import com.alexstyl.contactstore.ContactColumn.Mails
import com.alexstyl.contactstore.ContactColumn.Names
import com.alexstyl.contactstore.ContactColumn.Nickname
import com.alexstyl.contactstore.ContactColumn.Note
import com.alexstyl.contactstore.ContactColumn.Organization
import com.alexstyl.contactstore.ContactColumn.Phones
import com.alexstyl.contactstore.ContactColumn.PostalAddresses
import com.alexstyl.contactstore.ContactColumn.Relations
import com.alexstyl.contactstore.ContactColumn.SipAddresses
import com.alexstyl.contactstore.ContactColumn.WebAddresses

public interface Contact {
    public val contactId: Long
    public val displayName: String?

    public val lookupKey: LookupKey?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val prefix: String?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val firstName: String?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val middleName: String?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val lastName: String?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val suffix: String?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val phoneticFirstName: String?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val phoneticMiddleName: String?

    /**
     * Requires: [ContactColumn.Names]
     */
    public val phoneticLastName: String?

    /**
     * Requires: [ContactColumn.Nickname]
     */
    public val nickname: String?

    /**
     * Requires: [ContactColumn.GroupMemberships]
     */
    public val groups: List<GroupMembership>

    /**
     * Requires: [ContactColumn.Names]
     *
     * See [ContactsContract.FullNameStyle]
     */
    public val fullNameStyle: Int

    /**
     * Requires: [ContactColumn.Names]
     *
     * See [ContactsContract.PhoneticNameStyle]
     */
    public val phoneticNameStyle: Int

    /**
     * Requires: [ContactColumn.Image]
     *
     * *NOTE*: If you need the contact image for UI purposes, you probably need [Contact.imageUri] instead.
     * [ImageData] contains the raw [ByteArray] of the image, which might consume a lot of memory.
     */
    public val imageData: ImageData?

    /**
     * Requires: [ContactColumn.Phones]
     */
    public val phones: List<LabeledValue<PhoneNumber>>

    /**
     * Requires: [ContactColumn.SipAddresses]
     */
    public val sipAddresses: List<LabeledValue<SipAddress>>

    /**
     * Requires: [ContactColumn.Mails]
     */
    public val mails: List<LabeledValue<MailAddress>>

    /**
     * Requires: [ContactColumn.Events]
     */
    public val events: List<LabeledValue<EventDate>>

    /**
     * Requires: [ContactColumn.PostalAddresses]
     */
    public val postalAddresses: List<LabeledValue<PostalAddress>>

    /**
     * Requires: [ContactColumn.WebAddresses]
     */
    public val webAddresses: List<LabeledValue<WebAddress>>

    /**
     * Requires : [ContactColumn.LinkedAccountValues]
     */
    public val linkedAccountValues: List<LinkedAccountValue>

    /**
     * Requires : [ContactColumn.ImAddresses]
     */
    public val imAddresses: List<LabeledValue<ImAddress>>

    /**
     * Requires: [ContactColumn.Note]
     */
    public val note: com.alexstyl.contactstore.Note?

    /**
     * Requires: [ContactColumn.Organization]
     */
    public val organization: String?

    /**
     * Requires: [ContactColumn.Relations]
     */
    public val relations: List<LabeledValue<Relation>>

    /**
     * Requires: [ContactColumn.Organization]
     */
    public val jobTitle: String?
    public val isStarred: Boolean
    public val columns: List<ContactColumn>
}

public fun Contact.containsColumn(column: ContactColumn): Boolean {
    return columns.any { it == column }
}

public fun Contact.containsLinkedAccountColumns(): Boolean {
    return columns.any { it is LinkedAccountValues }
}

/**
 * Creates a copy of the Contact that can have its properties modified.
 *
 * Modifying the properties of the contact will not affect the stored contact of the device.
 * See [ContactStore] to learn how to persist your changes.
 */
public fun Contact.mutableCopy(builder: MutableContact.() -> Unit): MutableContact {
    return mutableCopy().apply(builder)
}

/**
 * Creates a copy of the Contact that can have its properties modified.
 *
 * Modifying the properties of the contact will not affect the stored contact of the device.
 * See [ContactStore] to learn how to persist your changes.
 */
public fun Contact.mutableCopy(): MutableContact {
    return MutableContact(
        contactId = contactId,
        firstName = if (containsColumn(Names)) firstName else null,
        organization = if (containsColumn(Organization)) organization else null,
        jobTitle = if (containsColumn(Organization)) jobTitle else null,
        lastName = if (containsColumn(Names)) lastName else null,
        isStarred = isStarred,
        imageData = if (containsColumn(Image)) imageData else null,
        phones = if (containsColumn(Phones)) phones.toMutableList() else mutableListOf(),
        mails = if (containsColumn(Mails)) mails.toMutableList() else mutableListOf(),
        events = if (containsColumn(Events)) events.toMutableList() else mutableListOf(),
        postalAddresses = if (containsColumn(PostalAddresses))
            postalAddresses.toMutableList()
        else
            mutableListOf(),
        webAddresses = if (containsColumn(WebAddresses))
            webAddresses.toMutableList()
        else
            mutableListOf(),
        sipAddresses = if (containsColumn(SipAddresses))
            sipAddresses.toMutableList()
        else
            mutableListOf(),
        imAddresses = if (containsColumn(ImAddresses)) imAddresses.toMutableList() else mutableListOf(),
        relations = if (containsColumn(Relations)) relations.toMutableList() else mutableListOf(),
        note = if (containsColumn(Note)) note else null,
        columns = columns,
        middleName = if (containsColumn(Names)) middleName else null,
        prefix = if (containsColumn(Names)) prefix else null,
        suffix = if (containsColumn(Names)) suffix else null,
        phoneticNameStyle = if (containsColumn(Names)) phoneticNameStyle else PhoneticNameStyle.UNDEFINED,
        phoneticFirstName = if (containsColumn(Names)) phoneticFirstName else null,
        phoneticLastName = if (containsColumn(Names)) phoneticLastName else null,
        phoneticMiddleName = if (containsColumn(Names)) phoneticMiddleName else null,
        groups = if (containsColumn(GroupMemberships)) groups.toMutableList() else mutableListOf(),
        fullNameStyle = if (containsColumn(Names)) fullNameStyle else FullNameStyle.UNDEFINED,
        nickname = if (containsColumn(Nickname)) nickname else null,
        linkedAccountValues = if (containsLinkedAccountColumns()) {
            linkedAccountValues
        } else {
            emptyList()
        },
    )
}

/**
 * Creates a [Uri] pointing to the image assigned to the contact
 */
public val Contact.imageUri: Uri
    get() {
        val contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
        return Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY)
    }
