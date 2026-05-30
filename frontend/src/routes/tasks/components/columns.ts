import type { ColumnDef } from "@tanstack/table-core";
import type { Task } from "../data/schemas";
import { renderComponent, renderSnippet } from "$lib/components/ui/data-table";
import { Checkbox } from "$lib/components/ui/checkbox";
import { DataTableColumnHeader } from "$lib/components/ui/data-table";
import { createRawSnippet } from "svelte";
import DataTableTitleCell from "./data-table-title-cell.svelte";
import DataTableStatusCell from "./data-table-status-cell.svelte";
import DataTablePriorityCell from "./data-table-priority-cell.svelte";
import DataTableRowActions from "./data-table-row-actions.svelte";

export const columns: ColumnDef<Task>[] = [
    {
        id: "select",
        header: ({ table }) =>
            renderComponent(Checkbox, {
                checked: table.getIsAllPageRowsSelected(),
                onCheckedChange: (value) => table.toggleAllPageRowsSelected(value),
                indeterminate:
                    table.getIsSomePageRowsSelected() && !table.getIsAllPageRowsSelected(),
                "aria-label": "Select all",
            }),
        cell: ({ row }) =>
            renderComponent(Checkbox, {
                checked: row.getIsSelected(),
                onCheckedChange: (value) => row.toggleSelected(value),
                "aria-label": "Select row",
            }),
        enableSorting: false,
        enableHiding: false,
    },
    {
        accessorKey: "id",
        header: ({ column }) => {
            return renderComponent(DataTableColumnHeader, {
                column,
                title: "Task",
            });
        },
        cell: ({ row }) => {
            const idSnippet = createRawSnippet<[{ id: string }]>((getId) => {
                const { id } = getId();
                return {
                    render: () => `<div class="w-[80px]">${id}</div>`,
                };
            });

            return renderSnippet(idSnippet, {
                id: row.original.id,
            });
        },
        enableSorting: false,
        enableHiding: false,
    },
    {
        accessorKey: "title",
        header: ({ column }) => renderComponent(DataTableColumnHeader, { column, title: "Title" }),
        cell: ({ row }) => {
            return renderComponent(DataTableTitleCell, {
                labelValue: row.original.label,
                value: row.original.title,
            });
        },
    },
    {
        accessorKey: "status",
        header: ({ column }) =>
            renderComponent(DataTableColumnHeader, {
                column,
                title: "Status",
            }),
        cell: ({ row }) => {
            return renderComponent(DataTableStatusCell, {
                value: row.original.status,
            });
        },
        filterFn: (row, id, value) => {
            return value.includes(row.getValue(id));
        },
    },
    {
        accessorKey: "priority",
        header: ({ column }) => {
            return renderComponent(DataTableColumnHeader, {
                title: "Priority",
                column,
            });
        },
        cell: ({ row }) => {
            return renderComponent(DataTablePriorityCell, {
                value: row.original.priority,
            });
        },
        filterFn: (row, id, value) => {
            return value.includes(row.getValue(id));
        },
    },
    {
        id: "actions",
        cell: ({ row }) => renderComponent(DataTableRowActions, { row }),
    },
];
