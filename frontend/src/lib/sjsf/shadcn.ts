import { setThemeContext } from '@sjsf/shadcn4-theme';
import {
  FieldDescription,
  FieldError,
  FieldLabel,
  Field,
  FieldSet,
  FieldLegend,
  FieldGroup,
  FieldTitle,
  ButtonGroup,
} from '@sjsf/shadcn4-theme/new-york';

import { Button } from '#lib/components/ui/button/index.js';
import { Checkbox } from '#lib/components/ui/checkbox/index.js';
import { Input } from '#lib/components/ui/input/index.js';
import { Select, SelectTrigger, SelectContent, SelectItem } from '#lib/components/ui/select/index.js';
import { Textarea } from '#lib/components/ui/textarea/index.js';

// https://x0k.dev/svelte-jsonschema-form/themes/shadcn4/#components
export function setShadcnThemeContext() {
  setThemeContext({
    components: {
      Button,
      FieldDescription,
      FieldError,
      FieldLabel,
      Field,
      FieldSet,
      FieldLegend,
      FieldGroup,
      FieldTitle,
      ButtonGroup,
      Checkbox,
      Input,
      Select,
      SelectTrigger,
      SelectContent,
      SelectItem,
      Textarea,

      // Popover,
      // PopoverContent,
      // PopoverTrigger,
      // Command,
      // CommandInput,
      // CommandList,
      // CommandEmpty,
      // CommandGroup,
      // CommandItem,
      // Calendar,
      // RangeCalendar,
      // ToggleGroup,
      // ToggleGroupItem,
      // RadioGroup,
      // RadioGroupItem,
      // Slider,
      // Switch,
      // FileDropZone,
      // FileDropZoneTrigger,
      // IPv4AddressInput,
      // NLPDateInput,
      // PasswordRoot,
      // PasswordInput,
      // PasswordStrength,
      // PasswordToggleVisibility,
      // PhoneInput,
      // StarRatingRoot,
      // StarRatingStar,
      // TagsInput
    },
  });
}
