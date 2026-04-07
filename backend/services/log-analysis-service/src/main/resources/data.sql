INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (1, 'Brute Force Attack Detected', 'Multiple failed login attempts.', 'HIGH', 'event.outcome', 'failure', 5, 10) ON CONFLICT (id) DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (1, 'Go to {{room}} immediately and find machine {{host}}.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (1, 'Physically disconnect the network cable from the back of the computer.') ON CONFLICT DO NOTHING;

INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (2, 'Potential Ransomware Detected', 'Massive file modification or system file access detected.', 'CRITICAL', 'event.category', 'file_tampering', 10, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (2, 'Unplug the power cable from machine {{host}} in {{room}}.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (2, 'DO NOT restart the computer.') ON CONFLICT DO NOTHING;

INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (3, 'Unauthorized User Creation', 'A new user account was created locally on a machine.', 'MEDIUM', 'event.action', 'user_created', 1, 0) ON CONFLICT (id) DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (3, 'Check machine {{host}} in {{room}} for unauthorized account creation.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (3, 'Verify whether this was a planned change.') ON CONFLICT DO NOTHING;

INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (4, 'USB Device Inserted', 'A removable USB device was connected to a machine.', 'MEDIUM', 'event.action', 'removable_media_inserted', 1, 0) ON CONFLICT (id) DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (4, 'Locate machine {{host}} in {{room}}.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (4, 'Ask the user to remove the USB device immediately.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (4, 'Inspect the device and verify no unauthorised files were transferred.') ON CONFLICT DO NOTHING;

INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (5, 'Mass File Deletion Detected', 'A large number of files were deleted in a short period, which may indicate ransomware or a malicious wipe.', 'HIGH', 'event.action', 'file_deleted', 20, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (5, 'Do NOT touch machine {{host}} in {{room}}.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (5, 'Disconnect the network cable from the back of the computer immediately.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (5, 'Do not restart or shut down the machine.') ON CONFLICT DO NOTHING;

INSERT INTO global_rule_templates (id, name, description, severity, field_to_watch, expected_value, threshold, window_minutes) VALUES (6, 'Scheduled Task Created', 'A new scheduled task or startup program was registered on a machine, which is a common way for malware to persist.', 'MEDIUM', 'event.action', 'scheduled_task_created', 1, 0) ON CONFLICT (id) DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (6, 'Check machine {{host}} in {{room}} — do not restart it.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (6, 'Verify whether any software was recently installed on this machine.') ON CONFLICT DO NOTHING;
INSERT INTO template_remediation_steps (template_id, step) VALUES (6, 'If no planned change was made, isolate the machine by unplugging the network cable.') ON CONFLICT DO NOTHING;

ALTER TABLE global_rule_templates ALTER COLUMN id RESTART WITH 7;
