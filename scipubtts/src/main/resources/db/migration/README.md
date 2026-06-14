Quy uoc Flyway cho project nay

1. Khong sua migration cu da tung chay tren moi truong dung chung.
2. Moi thay doi schema phai tao file migration moi, khong sua de len file cu.
3. Dat version theo timestamp de tranh trung version giua cac branch.

Format khuyen dung:

VYYYYMMDDHHMMSS__short_description.sql

Vi du:

V20260613161000__add_saved_search_indexes.sql
V20260613164500__create_user_preferences_table.sql

Vi sao project bat `out-of-order`:

- Neu branch A len DB truoc voi version cao hon, branch B merge sau voi version thap hon,
  Flyway van co the chay phan migration con thieu thay vi fail ngay luc startup.

Vi sao project bat `ignore future migration`:

- Team dang dung chung 1 Supabase DB.
- Neu DB da co migration moi hon branch local cua ban, app local van co the chay tam.
- Cau hinh nay chi giup local "do fail", khong thay the viec pull/rebase code con thieu.

Quy trinh team nen theo:

1. Truoc khi tao migration moi, pull code moi nhat.
2. Tao file migration moi bang timestamp hien tai.
3. Neu branch khac vua them migration, pull/rebase truoc khi merge.
4. Neu van can sua schema, tao them 1 migration moi, khong quay lai sua file cu.

Khi Flyway bao loi thi xu ly nhu sau:

- DB shared da co migration moi hon branch cua ban:
  Pull/rebase file migration con thieu truoc. Neu ban con thay doi schema, tao 1 file moi voi timestamp lon hon.

- Duplicate version:
  2 file khac nhau nhung cung version.
  Doi ten file moi chua chay thanh timestamp khac truoc khi merge.

- Checksum mismatch:
  Nghia la file migration cu da bi sua sau khi tung chay.
  Khong sua tiep file cu. Hay khoi phuc file do tu git history, roi tao 1 migration moi neu can fix.

- History table da dung nhung local file bi sua nham:
  Restore lai dung noi dung file tu git history.
  Chi dung `flyway repair` khi ban chac chan DB va lich su migration hien tai da dung san.
