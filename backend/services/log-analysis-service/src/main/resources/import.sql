INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (1, 'Brute Force Attack Detected', 'Multiple failed login attempts.', 'HIGH', 'event.outcome', 'failure', 5, 10);
INSERT INTO template_remediation_steps (template_id, step) VALUES (1, 'Go to {{room}} immediately and find machine {{host}}.');
INSERT INTO template_remediation_steps (template_id, step) VALUES (1, 'Physically disconnect the network cable from the back of the computer.');
INSERT INTO template_remediation_steps (template_id, step) VALUES (1, 'Call the Council IT desk at {{council_phone}} and report a brute force attempt.');

INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (2, 'Potential Ransomware Detected', 'Massive file modification or system file access detected.', 'CRITICAL', 'event.category', 'file_tampering', 10, 1);
INSERT INTO template_remediation_steps (template_id, step) VALUES (2, 'Unplug the power cable from machine {{host}} in {{room}}.');
INSERT INTO template_remediation_steps (template_id, step) VALUES (2, 'DO NOT restart the computer.');
INSERT INTO template_remediation_steps (template_id, step) VALUES (2, 'Call the Council IT line at {{council_phone}} immediately.');

INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (3, 'Unauthorized User Creation', 'A new user account was created locally on a machine.', 'MEDIUM', 'event.action', 'user_created', 1, 0);
INSERT INTO template_remediation_steps (template_id, step) VALUES (3, 'Check machine {{host}} in {{room}} for unauthorized account creation.');
INSERT INTO template_remediation_steps (template_id, step) VALUES (3, 'Verify with the Council IT team if this was a planned change.');

SELECT setval(pg_get_serial_sequence('global_rule_templates', 'id'), 3);
