import { renderComponent } from "$lib/components/ui/data-table";
import type { ColumnDef } from "@tanstack/table-core";
import { DataTableColumnHeader } from "$lib/components/ui/data-table";
import DataTableActions from "./data-table-actions.svelte";
import { Checkbox } from "$lib/components/ui/checkbox";
import { z } from "zod";

export const userSchema = z.object({
    id: z.string(),
    name: z.string().max(100),
    email: z.email(),
    website: z.url().optional(),
    country: z.string().length(2),
});

export type User = z.infer<typeof userSchema>;

export const columns: ColumnDef<User>[] = [
    {
        id: "select",
        header: ({ table }) =>
        renderComponent(Checkbox, {
            checked: table.getIsAllPageRowsSelected(),
            indeterminate:
                table.getIsSomePageRowsSelected() &&
                !table.getIsAllPageRowsSelected(),
            onCheckedChange: (value) => table.toggleAllPageRowsSelected(!!value),
            "aria-label": "Select all",
        }),
        cell: ({ row }) =>
        renderComponent(Checkbox, {
            checked: row.getIsSelected(),
            onCheckedChange: (value) => row.toggleSelected(!!value),
            "aria-label": "Select row",
        }),
        enableSorting: false,
        enableHiding: false,
    },
    {
        accessorKey: "name",
        header: ({ column }) =>
            renderComponent(DataTableColumnHeader, {
                title: "Name",
                column: column
            })
    },
    {
        accessorKey: "email",
        header: ({ column }) =>
            renderComponent(DataTableColumnHeader, {
                title: "Email",
                column: column
            })
    },
    {
        accessorKey: "website",
        header: ({ column }) =>
            renderComponent(DataTableColumnHeader, {
                title: "Website",
                column: column
            })
    },
    {
        accessorKey: "country",
        header: ({ column }) =>
            renderComponent(DataTableColumnHeader, {
                title: "Country",
                column: column
            })
    },
    {
        id: "actions",
        enableSorting: false,
        cell: ({ row }) => renderComponent(DataTableActions, { id: row.original.id })
    }
];
