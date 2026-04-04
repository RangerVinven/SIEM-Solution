INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome) VALUES (1, 'windows.event', '4624', 'authentication', 'logon', 'success');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome) VALUES (2, 'windows.event', '4625', 'authentication', 'logon', 'failure');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome) VALUES (3, 'windows.event', '4663', 'file_tampering', 'access', 'success');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome) VALUES (4, 'windows.event', '4660', 'file_tampering', 'delete', 'success');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome) VALUES (5, 'windows.event', '4720', 'iam', 'user_created', 'success');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome) VALUES (6, 'windows.event', '4732', 'iam', 'privilege_escalation', 'success');

SELECT setval(pg_get_serial_sequence('normalisation_mappings', 'id'), 6);
