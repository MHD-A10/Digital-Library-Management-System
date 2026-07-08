package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import java.awt.ComponentOrientation;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    public enum Language {
        AR,
        EN
    }

    private static Language currentLanguage = Language.AR;
    private static final Map<String, String> ar = new HashMap<>();
    private static final Map<String, String> en = new HashMap<>();

    static {
        put("app.title", "نظام إدارة المكتبة", "Library Management System");
        put("app.name", "المكتبة", "Library");
        put("welcome", "مرحبا بك في نظام إدارة المكتبة", "Welcome to the Library Management System");

        put("nav.home", "الرئيسية", "Home");
        put("nav.books", "إدارة الكتب", "Books");
        put("nav.borrowers", "المستعيرون", "Borrowers");
        put("nav.borrowing", "الإعارة والإرجاع", "Borrowing & Returns");
        put("nav.waitlist", "قائمة الانتظار", "Wait List");
        put("nav.reports", "التقارير", "Reports");
        put("nav.exit", "خروج", "Exit");

        put("language.toggle", "English", "العربية");
        put("language.tooltip", "تغيير اللغة", "Change language");

        put("dialog.confirm", "تأكيد", "Confirm");
        put("dialog.exit.title", "تأكيد الخروج", "Exit Confirmation");
        put("dialog.exit.message", "هل تريد الخروج من البرنامج؟", "Do you want to exit the program?");
        put("placeholder.page", "صفحة", "Page");
        put("placeholder.building", "قيد البناء", "under construction");
        put("placeholder.error", "خطأ", "Error");
        put("books.title", "إدارة الكتب", "Books Management");
        put("books.filter.all", "الكل", "All");
        put("books.filter.title", "العنوان", "Title");
        put("books.filter.author", "المؤلف", "Author");
        put("books.search.all", "بحث (ISBN، عنوان، مؤلف)...", "Search (ISBN, title, author)...");
        put("books.search.isbn", "أدخل ISBN (مثال: 111-222)", "Enter ISBN (example: 111-222)");
        put("books.search.title", "أدخل عنوان الكتاب...", "Enter book title...");
        put("books.search.author", "أدخل اسم المؤلف...", "Enter author name...");
        put("books.add", "إضافة", "Add");
        put("books.updateCopies", "تعديل النسخ", "Update Copies");
        put("books.delete", "حذف", "Delete");
        put("books.tree", "شجرة AVL", "AVL Tree");
        put("books.refresh", "تحديث", "Refresh");
        put("books.refreshed", "تم التحديث", "Updated");
        put("books.add.success", "تم إضافة الكتاب بنجاح", "Book added successfully");
        put("books.add.fail", "فشل الإضافة: ISBN موجود مسبقا أو بيانات غير صحيحة", "Add failed: ISBN already exists or data is invalid");
        put("books.selectFirst", "اختر كتابا من الجدول أولا", "Select a book from the table first");
        put("books.update.title", "تعديل عدد النسخ", "Update Copies");
        put("books.update.success", "تم تحديث عدد النسخ", "Copies updated successfully");
        put("books.update.fail", "لا يمكن تقليل النسخ إلى أقل من المستعار حاليا", "Cannot reduce copies below currently borrowed copies");
        put("books.delete.confirm.title", "تأكيد الحذف", "Delete Confirmation");
        put("books.delete.confirm", "هل أنت متأكد من حذف الكتاب:", "Are you sure you want to delete this book:");
        put("books.delete.success", "تم حذف الكتاب", "Book deleted successfully");
        put("books.delete.fail", "لا يمكن الحذف: هناك نسخ مستعارة حاليا", "Cannot delete: some copies are currently borrowed");
        put("books.notification", "إشعار", "Notification");
        put("books.stats.titles", "إجمالي العناوين", "Titles");
        put("books.stats.copies", "إجمالي النسخ", "Copies");
        put("books.stats.available", "المتوفرة", "Available");
        put("books.stats.borrowed", "المستعارة", "Borrowed");
        put("books.col.isbn", "ISBN", "ISBN");
        put("books.col.title", "العنوان", "Title");
        put("books.col.author", "المؤلف", "Author");
        put("books.col.total", "الكلي", "Total");
        put("books.col.available", "المتوفر", "Available");
        put("books.col.borrowed", "المستعار", "Borrowed");
        put("books.col.borrowCount", "مرات الاستعارة", "Borrow Count");
        put("bookForm.title", "إضافة كتاب جديد", "Add New Book");
        put("bookForm.isbn.placeholder", "مثال: 111-222 (أرقام فقط)", "Example: 111-222 (digits only)");
        put("bookForm.title.placeholder", "عنوان الكتاب", "Book title");
        put("bookForm.author.placeholder", "اسم المؤلف", "Author name");
        put("bookForm.label.title", "عنوان الكتاب:", "Book Title:");
        put("bookForm.label.author", "اسم المؤلف:", "Author Name:");
        put("bookForm.label.copies", "عدد النسخ:", "Copies Count:");
        put("bookForm.save", "حفظ", "Save");
        put("bookForm.cancel", "إلغاء", "Cancel");
        put("bookForm.warning", "تنبيه", "Warning");
        put("bookForm.isbnRequired", "حقل ISBN مطلوب", "ISBN is required");
        put("bookForm.invalidIsbnTitle", "خطأ في ISBN", "ISBN Error");
        put("bookForm.invalidIsbn", "صيغة ISBN غير صحيحة\nيجب أن تكون: XXX-XXX (أرقام فقط)\nمثال: 111-222", "Invalid ISBN format\nIt must be: XXX-XXX (digits only)\nExample: 111-222");
        put("bookForm.titleRequired", "حقل العنوان مطلوب", "Book title is required");
        put("bookForm.authorRequired", "حقل المؤلف مطلوب", "Author name is required");
        put("bookForm.copiesRequired", "عدد النسخ يجب أن يكون بين 1 و 999", "Copies count must be between 1 and 999");
        put("borrowers.title", "إدارة المستعيرين", "Borrowers Management");
        put("borrowers.filter.all", "الكل", "All");
        put("borrowers.filter.id", "الرقم", "ID");
        put("borrowers.filter.name", "الاسم", "Name");
        put("borrowers.filter.type", "النوع", "Type");
        put("borrowers.search.all", "بحث (رقم، اسم، نوع)...", "Search (ID, name, type)...");
        put("borrowers.search.id", "أدخل رقم المستعير...", "Enter borrower ID...");
        put("borrowers.search.name", "أدخل اسم المستعير...", "Enter borrower name...");
        put("borrowers.search.type", "اكتب: طالب أو متخرج", "Type: student or graduate");
        put("borrowers.add", "إضافة مستعير", "Add Borrower");
        put("borrowers.active", "الاستعارات النشطة", "Active Borrows");
        put("borrowers.refresh", "تحديث", "Refresh");
        put("borrowers.refreshed", "تم التحديث", "Updated");
        put("borrowers.add.success", "تم إضافة المستعير بنجاح", "Borrower added successfully");
        put("borrowers.add.fail", "فشل الإضافة: الرقم موجود مسبقا أو بيانات غير صحيحة", "Add failed: ID already exists or data is invalid");
        put("borrowers.selectFirst", "اختر مستعيرا من الجدول أولا", "Select a borrower from the table first");
        put("borrowers.noActive", "لا توجد استعارات نشطة للمستعير:", "No active borrows for borrower:");
        put("borrowers.active.title", "الاستعارات النشطة", "Active Borrows");
        put("borrowers.active.for", "الاستعارات النشطة لـ", "Active borrows for");
        put("borrowers.borrowDate", "تاريخ الاستعارة", "Borrow date");
        put("borrowers.expectedReturn", "الإرجاع المتوقع", "Expected return");
        put("borrowers.recordId", "رقم السجل", "Record ID");
        put("borrowers.notification", "إشعار", "Notification");
        put("borrowers.type.student", "طالب", "Student");
        put("borrowers.type.graduate", "متخرج", "Graduate");
        put("borrowers.stats.total", "إجمالي المستعيرين", "Borrowers");
        put("borrowers.stats.graduates", "متخرجون", "Graduates");
        put("borrowers.stats.students", "طلاب", "Students");
        put("borrowers.stats.active", "إجمالي الاستعارات النشطة", "Active borrows");
        put("borrowers.col.id", "الرقم", "ID");
        put("borrowers.col.name", "الاسم", "Name");
        put("borrowers.col.type", "النوع", "Type");
        put("borrowers.col.active", "الاستعارات النشطة", "Active Borrows");
        put("borrowerForm.title", "إضافة مستعير جديد", "Add New Borrower");
        put("borrowerForm.heading", "بيانات المستعير", "Borrower Information");
        put("borrowerForm.id.placeholder", "مثال: S1001", "Example: S1001");
        put("borrowerForm.name.placeholder", "الاسم الكامل", "Full name");
        put("borrowerForm.label.id", "رقم المستعير:", "Borrower ID:");
        put("borrowerForm.label.name", "الاسم:", "Name:");
        put("borrowerForm.label.type", "النوع:", "Type:");
        put("borrowerForm.student", "طالب", "Student");
        put("borrowerForm.graduate", "متخرج (أولوية أعلى)", "Graduate (higher priority)");
        put("borrowerForm.hint", "ملاحظة: المتخرج يحصل على أولوية في قوائم الانتظار.", "Note: graduates get priority in wait lists.");
        put("borrowerForm.save", "حفظ", "Save");
        put("borrowerForm.cancel", "إلغاء", "Cancel");
        put("borrowerForm.error", "خطأ", "Error");
        put("borrowerForm.idRequired", "رقم المستعير مطلوب.", "Borrower ID is required.");
        put("borrowerForm.nameRequired", "اسم المستعير مطلوب.", "Borrower name is required.");
        put("borrowing.title", "الإعارة والإرجاع", "Borrowing & Returns");
        put("borrowing.new", "إعارة جديدة", "New Borrow");
        put("borrowing.stats.total", "إجمالي العمليات", "Total Records");
        put("borrowing.stats.active", "نشطة", "Active");
        put("borrowing.stats.overdue", "متأخرة", "Overdue");
        put("borrowing.stats.returned", "معادة", "Returned");
        put("borrowing.search", "بحث: رقم السجل / اسم / ISBN / عنوان", "Search: record ID / name / ISBN / title");
        put("borrowing.filter.all", "الكل", "All");
        put("borrowing.filter.active", "النشطة", "Active");
        put("borrowing.filter.overdue", "المتأخرة", "Overdue");
        put("borrowing.filter.returned", "المعادة", "Returned");
        put("borrowing.borrow.success", "تمت عملية الإعارة بنجاح", "Borrow operation completed successfully");
        put("borrowing.alreadyReturned", "هذا السجل معاد مسبقا", "This record is already returned");
        put("borrowing.return.confirm", "هل تريد إرجاع:", "Do you want to return:");
        put("borrowing.return.confirmTitle", "تأكيد الإرجاع", "Return Confirmation");
        put("borrowing.return.success", "تم الإرجاع بنجاح", "Book returned successfully");
        put("borrowing.return.fail", "فشل الإرجاع", "Return failed");
        put("borrowing.overdueAlert.prefix", "لديك", "You have");
        put("borrowing.overdueAlert.suffix", "إعارة متأخرة!", "overdue borrow records!");
        put("borrowing.action.return", "إرجاع", "Return");
        put("borrowing.status.active", "نشطة", "Active");
        put("borrowing.status.overdue", "متأخرة", "Overdue");
        put("borrowing.status.returned", "معادة", "Returned");
        put("borrowing.col.record", "#", "#");
        put("borrowing.col.borrower", "المستعير", "Borrower");
        put("borrowing.col.book", "الكتاب", "Book");
        put("borrowing.col.borrowDate", "تاريخ الإعارة", "Borrow Date");
        put("borrowing.col.dueDate", "تاريخ الاستحقاق", "Due Date");
        put("borrowing.col.status", "الحالة", "Status");
        put("borrowing.col.action", "إجراء", "Action");

        put("borrowForm.title", "إعارة كتاب", "Borrow Book");
        put("borrowForm.borrowerLabel", "أدخل رقم المستعير:", "Enter borrower ID:");
        put("borrowForm.isbnLabel", "أدخل ISBN الكتاب:", "Enter book ISBN:");
        put("borrowForm.save", "حفظ", "Save");
        put("borrowForm.cancel", "إلغاء", "Cancel");
        put("borrowForm.preview.active", "نشطة", "Active");
        put("borrowForm.preview.graduate", "تخرج", "Graduate");
        put("borrowForm.preview.borrowerMissing", "لا يوجد مستعير بهذا الرقم", "No borrower with this ID");
        put("borrowForm.preview.available", "متاح", "Available");
        put("borrowForm.preview.bookMissing", "لا يوجد كتاب بهذا ISBN", "No book with this ISBN");
        put("borrowForm.success.title", "نجاح", "Success");
        put("borrowForm.success", "تمت الإعارة بنجاح", "Borrowed successfully");
        put("borrowForm.waitlist.title", "قائمة الانتظار", "Wait List");
        put("borrowForm.waitlisted", "الكتاب غير متاح. تمت إضافتك لقائمة الانتظار", "Book is unavailable. Borrower was added to the wait list");
        put("borrowForm.warning", "تنبيه", "Warning");
        put("borrowForm.alreadyWaitlisted", "أنت بالفعل في قائمة الانتظار لهذا الكتاب", "Borrower is already waiting for this book");
        put("borrowForm.alreadyBorrowed", "هذا المستعير قد استعار هذا الكتاب مسبقا", "This borrower already borrowed this book");
        put("borrowForm.limitReached", "وصل المستعير للحد الأقصى من الاستعارات", "Borrower reached the maximum borrow limit");
        put("borrowForm.notAvailable", "لا توجد نسخ متاحة من هذا الكتاب حاليا", "No copies are currently available for this book");
        put("borrowForm.invalid", "بيانات غير صالحة", "Invalid input");
        put("borrowForm.error", "خطأ", "Error");

        put("waitlist.title", "قائمة الانتظار", "Wait List");
        put("waitlist.subtitle", "متابعة طلبات الانتظار على الكتب غير المتاحة", "Track requests for unavailable books");
        put("waitlist.stats.total", "إجمالي الطلبات", "Total Requests");
        put("waitlist.stats.books", "كتب لها قوائم", "Books With Queues");
        put("waitlist.stats.heads", "أوائل الطوابير", "Queue Heads");
        put("waitlist.stats.waiting", "بانتظار الدور", "Waiting Turn");
        put("waitlist.refresh", "تحديث", "Refresh");
        put("waitlist.updated", "تم تحديث قائمة الانتظار", "Wait list updated");
        put("waitlist.filter.all", "الكل", "All");
        put("waitlist.filter.head", "أول في الطابور", "Queue Head");
        put("waitlist.filter.rest", "بقية الطابور", "Waiting");
        put("waitlist.search", "بحث باسم أو رقم المستعير", "Search by borrower name or ID");
        put("waitlist.allBooks", "كل الكتب", "All Books");
        put("waitlist.col.position", "الترتيب", "Position");
        put("waitlist.col.book", "الكتاب", "Book");
        put("waitlist.col.isbn", "ISBN", "ISBN");
        put("waitlist.col.borrower", "المستعير", "Borrower");
        put("waitlist.col.time", "وقت الطلب", "Request Time");
        put("waitlist.col.status", "الحالة", "Status");
        put("waitlist.status.head", "أول في الطابور", "Queue Head");
        put("waitlist.status.waiting", "بانتظار الدور", "Waiting");

        put("avl.dialogTitle", "تصور شجرة الكتب (AVL مقابل BST)", "Book Tree Visualizer (AVL vs BST)");
        put("avl.title", "تصور شجرة الكتب", "Book Tree Visualizer");
        put("avl.actualTitle", "شجرة AVL الفعلية (متوازنة)", "Actual AVL Tree (Balanced)");
        put("avl.bstTitle", "شجرة BST عادية بدون توازن (نفس الكتب)", "Regular BST Without Balancing (Same Books)");
        put("avl.avlButton", "شجرة AVL (متوازنة)", "AVL Tree (Balanced)");
        put("avl.bstButton", "شجرة BST (بدون توازن)", "BST Tree (Unbalanced)");
        put("avl.refresh", "تحديث", "Refresh");
        put("avl.updated", "تم التحديث", "Updated");
        put("avl.close", "إغلاق", "Close");
        put("avl.booksCount", "عدد الكتب", "Books Count");
        put("avl.avlHeight", "ارتفاع AVL", "AVL Height");
        put("avl.bstHeight", "ارتفاع BST", "BST Height");
        put("avl.diff", "الفرق (مستوى)", "Difference (Levels)");
        put("avl.empty", "لا توجد كتب بعد لعرضها - أضف كتبا من واجهة إدارة الكتب", "No books to display yet - add books from the Books page");
        put("avl.tooltip.title", "العنوان", "Title");
        put("avl.tooltip.height", "الارتفاع", "Height");
        put("reports.title", "التقارير والإحصائيات", "Reports & Analytics");
        put("reports.subtitle", "نظرة شاملة على أداء المكتبة", "A complete overview of library activity");
        put("reports.refresh", "تحديث", "Refresh");
        put("reports.copy", "نسخ", "Copy");
        put("reports.exportReport", "تصدير تقرير", "Export Report");
        put("reports.saveData", "حفظ البيانات", "Save Data");
        put("reports.importData", "استيراد البيانات", "Import Data");
        put("reports.books", "الكتب", "Books");
        put("reports.borrowers", "المستعيرون", "Borrowers");
        put("reports.records", "سجلات الإعارة", "Borrow Records");
        put("reports.overdue", "المتأخرة", "Overdue");
        put("reports.borrowStatus", "حالة سجلات الإعارة", "Borrow Records Status");
        put("reports.copyDistribution", "توزيع النسخ", "Copies Distribution");
        put("reports.fullReport", "التقرير الكامل", "Full Report");
        put("reports.updated", "تم تحديث التقرير", "Report updated");
        put("reports.save.success", "تم حفظ بيانات المكتبة بنجاح", "Library data saved successfully");
        put("reports.save.fail", "فشل حفظ بيانات المكتبة", "Failed to save library data");
        put("reports.import.confirm", "سيتم استبدال البيانات الحالية بالبيانات الموجودة في الملف. هل تريد المتابعة؟", "Current data will be replaced by the selected file data. Continue?");
        put("reports.import.title", "تأكيد الاستيراد", "Import Confirmation");
        put("reports.import.success", "تم استيراد بيانات المكتبة بنجاح", "Library data imported successfully");
        put("reports.import.fail", "فشل استيراد البيانات. تأكد من أن الملف صحيح وغير معدل بطريقة خاطئة.", "Failed to import data. Make sure the file is valid and not edited incorrectly.");
        put("reports.export.success", "تم تصدير التقرير بنجاح", "Report exported successfully");
        put("reports.export.fail", "فشل تصدير التقرير", "Failed to export report");
        put("reports.copy.success", "تم نسخ التقرير إلى الحافظة", "Report copied to clipboard");
        put("reports.copy.fail", "تعذر النسخ", "Copy failed");
        put("reports.chart.active", "نشطة", "Active");
        put("reports.chart.overdue", "متأخرة", "Overdue");
        put("reports.chart.returned", "معادة", "Returned");
        put("reports.chart.available", "متاحة", "Available");
        put("reports.chart.borrowed", "مستعارة", "Borrowed");
        put("reports.noData", "لا توجد بيانات", "No data");
        put("reports.totalCopies", "إجمالي النسخ", "Total Copies");
        }

    private static void put(String key, String arabic, String english) {
        ar.put(key, arabic);
        en.put(key, english);
    }

    public static String text(String key) {
        Map<String, String> dictionary = currentLanguage == Language.AR ? ar : en;
        return dictionary.getOrDefault(key, key);
    }

    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    public static boolean isArabic() {
        return currentLanguage == Language.AR;
    }

    public static void toggleLanguage() {
        currentLanguage = currentLanguage == Language.AR ? Language.EN : Language.AR;
    }

    public static ComponentOrientation orientation() {
        return isArabic() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT;
    }
}