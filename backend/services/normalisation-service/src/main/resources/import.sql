INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome, target_level) VALUES (1, 'windows.event', '4624', 'authentication', 'logon', 'success', 'information');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome, target_level) VALUES (2, 'windows.event', '4625', 'authentication', 'logon', 'failure', 'warning');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome, target_level) VALUES (3, 'windows.event', '4663', 'file_tampering', 'access', 'success', 'information');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome, target_level) VALUES (4, 'windows.event', '4660', 'file_tampering', 'delete', 'success', 'warning');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome, target_level) VALUES (5, 'windows.event', '4720', 'iam', 'user_created', 'success', 'information');
INSERT INTO normalisation_mappings (id, source_dataset, source_id, target_category, target_action, target_outcome, target_level) VALUES (6, 'windows.event', '4732', 'iam', 'privilege_escalation', 'success', 'warning');

SELECT setval(pg_get_serial_sequence('normalisation_mappings', 'id'), 6);
