-- MariaDB manual migration for certificate schema cleanup.
-- Run once after deploying the code that removed duplicate_key from NationalCertificate.

ALTER TABLE national_certificate
    DROP INDEX uk_certificate_duplicate_key;

ALTER TABLE national_certificate
    DROP COLUMN duplicate_key;
