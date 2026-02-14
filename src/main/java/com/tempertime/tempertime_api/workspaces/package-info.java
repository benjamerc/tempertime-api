/**
 * Workspace module.
 *
 * Provides domain entities, DTOs, mappers, repositories, and services
 * for managing workspaces, workspace memberships, roles, and invite codes.
 *
 * Includes functionality for:
 * - Creating, updating, and deleting workspaces.
 * - Managing workspace memberships and user roles.
 * - Generating, activating, deactivating, and regenerating workspace invite codes.
 * - Enforcing access control and role-based authorization within workspaces.
 *
 * Notes (in workspace context):
 * - "users" refers to workspace participants within a workspace.
 * - "members" refers to users with the MEMBER role in the workspace.
 * - "membership" is the relationship between a workspace and its users,
 *   represented by the WorkspaceUser entity.
 */
package com.tempertime.tempertime_api.workspaces;